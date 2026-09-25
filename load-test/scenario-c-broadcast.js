// 시나리오 C: 답변 수정·삭제 브로드캐스트 수신률
//
// 실행 예:
//   k6 run -e VERSION=after -e PORT=8082 -e JWT_SECRET=<로컬 yml의 jwt.secret> -e RUN=1 load-test/scenario-c-broadcast.js
//
// 구성 (load-test/seed-c.sql):
//   방 5개, 방마다 작성자(TEAM) 2명 + 구독자(GUEST) 20명
//   - 구독자: 방 토픽을 구독하고 UPDATE/DELETE 이벤트 수신 건수를 센다.
//   - 작성자: 방 토픽과 에러 큐를 구독하고 [생성 → 수정 → 삭제]를 CYCLE_MS 간격으로 반복한다.
//            수정·삭제마다 REST로 DB 반영 여부를 확인해 "실제로 반영된 작업" 기준으로 기대 수신 건수를 계산한다.
//
// 지표:
//   broadcast_receipt_rate = 구독자 수신 건수 / (DB 반영된 수정·삭제 수 × 방 구독자 수)
//   silent_failure         = DB에는 반영됐는데 작성자가 브로드캐스트를 받지 못한 수정·삭제 수
//   requester_error{op,code} = 작성자 에러 큐로 받은 에러 코드 분포

import { WebSocket } from 'k6/websockets';
import { setTimeout, clearTimeout } from 'k6/timers';
import { Counter } from 'k6/metrics';
import exec from 'k6/execution';
import http from 'k6/http';
import crypto from 'k6/crypto';
import encoding from 'k6/encoding';

const VERSION = __ENV.VERSION;
const HOST = __ENV.HOST || 'localhost';
const PORT = __ENV.PORT;
const JWT_SECRET = __ENV.JWT_SECRET;
const RUN = __ENV.RUN || '0';

if (!['before', 'after'].includes(VERSION) || !PORT || !JWT_SECRET) {
    throw new Error('VERSION(before|after), PORT, JWT_SECRET 환경변수가 필요합니다.');
}

const WS_URL = `ws://${HOST}:${PORT}/ws/websocket`;
const HTTP_URL = `http://${HOST}:${PORT}`;

// ===== 측정 조건 (seed-c.sql 과 일치해야 함) =====

const ROOMS = 5;
const WRITERS_PER_ROOM = 2;
const SUBSCRIBERS_PER_ROOM = 20;
const QUESTIONS_PER_WRITER = 10;

const WARMUP_S = 10;          // 구독자 연결·구독 완료 대기
const MEASURE_S = Number(__ENV.MEASURE_S || 60);         // 작성자 작업 시간
const GRACE_S = 5;            // 마지막 작업의 브로드캐스트가 구독자에게 도착할 여유
const CYCLE_MS = 1000;        // 작성자 1명의 [생성→수정→삭제] 주기 (질문 10개를 돌려 써서 질문당 0.1회/초)
const OUTCOME_TIMEOUT_MS = 3000;

const roomId = (r) => 2000 + r;
const writerId = (r, w) => 20000 + r * 100 + w;
const subscriberId = (r, s) => 20000 + r * 100 + 10 + s;
const questionId = (r, w, q) => 200000 + r * 1000 + w * 100 + q;

const OPS = ['create', 'update', 'delete'];
const KNOWN_CODES = [
    'SOCKET-1001', 'SOCKET-1002', 'SOCKET-1003', 'SOCKET-2000', 'SOCKET-3000',
    'ANSWER-001', 'ANSWER-002', 'ANSWER-003', 'ANSWER-004', 'ANSWER-005', 'ANSWER-006', 'ANSWER-007',
    'AUTH-001', 'AUTH-002', 'PARTICIPANT-001', 'QUESTION-001', 'QUESTION-002', 'OTHER',
];

export const options = {
    scenarios: {
        subscribers: {
            executor: 'per-vu-iterations',
            exec: 'subscriber',
            vus: ROOMS * SUBSCRIBERS_PER_ROOM,
            iterations: 1,
            maxDuration: `${WARMUP_S + MEASURE_S + GRACE_S + 30}s`,
        },
        writers: {
            executor: 'per-vu-iterations',
            exec: 'writer',
            vus: ROOMS * WRITERS_PER_ROOM,
            iterations: 1,
            startTime: `${WARMUP_S}s`,
            maxDuration: `${MEASURE_S + GRACE_S + 30}s`,
        },
    },
    // 태그별 서브메트릭을 handleSummary 데이터에 포함시키기 위한 임계값(판정용 아님)
    thresholds: buildTagThresholds(),
};

// ===== 메트릭 =====

const opSent = new Counter('op_sent');                   // {op}
const opApplied = new Counter('op_applied');             // {op} DB 반영 확인
const opBroadcastSeen = new Counter('op_broadcast_seen'); // {op} 작성자가 자기 작업의 브로드캐스트를 받음
const opTimeout = new Counter('op_timeout');             // {op} 브로드캐스트도 에러도 없음
const silentFailure = new Counter('silent_failure');     // {op} 반영됐는데 브로드캐스트 없음
const requesterError = new Counter('requester_error');   // {op, code}
const broadcastExpected = new Counter('broadcast_expected'); // {op} 반영된 작업 × 방 구독자 수
const broadcastReceived = new Counter('broadcast_received'); // {op} 구독자 수신 건수
const subscriberReady = new Counter('subscriber_ready');

function buildTagThresholds() {
    const t = {};
    OPS.forEach((op) => {
        ['op_sent', 'op_applied', 'op_broadcast_seen', 'op_timeout', 'silent_failure',
            'broadcast_expected', 'broadcast_received'].forEach((m) => { t[`${m}{op:${op}}`] = ['count>=0']; });
        KNOWN_CODES.forEach((code) => { t[`requester_error{op:${op},code:${code}}`] = ['count>=0']; });
    });
    return t;
}

// ===== 버전별 경로·payload 분기 =====

const isBefore = VERSION === 'before';
const payload = (body, memberId) => (isBefore ? { ...body, memberId } : body);
const createPath = (r, q) => `/app/rooms/${r}/question/${q}/answers/create`;
const updatePath = (r, a) => (isBefore ? `/app/answers/${a}/update` : `/app/rooms/${r}/answers/${a}/update`);
const deletePath = (r, a) => (isBefore ? `/app/answers/${a}/delete` : `/app/rooms/${r}/answers/${a}/delete`);

// ===== JWT (서버 JwtTokenProvider와 같은 형식) =====

function jwt(memberId, role) {
    const now = Math.floor(Date.now() / 1000);
    const header = encoding.b64encode(JSON.stringify({ alg: 'HS256', typ: 'JWT' }), 'rawurl');
    const body = encoding.b64encode(JSON.stringify({
        sub: String(memberId), nickname: `lt${memberId}`, role, iat: now, exp: now + 3600,
    }), 'rawurl');
    const signature = crypto.hmac('sha256', JWT_SECRET, `${header}.${body}`, 'base64rawurl');
    return `${header}.${body}.${signature}`;
}

// ===== STOMP =====

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

function frame(command, headers, body = '') {
    const lines = Object.entries(headers).map(([k, v]) => `${k}:${v}`);
    return `${command}\n${lines.join('\n')}\n\n${body}\0`;
}

function parseFrames(data) {
    return data.split('\0')
        .map((raw) => raw.replace(/^\n+/, ''))
        .filter((raw) => raw.length > 0)
        .map((raw) => {
            const sep = raw.indexOf('\n\n');
            const head = sep >= 0 ? raw.substring(0, sep) : raw;
            const body = sep >= 0 ? raw.substring(sep + 2) : '';
            const [command, ...headerLines] = head.split('\n');
            const headers = {};
            headerLines.forEach((line) => {
                const i = line.indexOf(':');
                if (i > 0 && !(line.substring(0, i) in headers)) {
                    headers[line.substring(0, i)] = line.substring(i + 1);
                }
            });
            let json = null;
            try { json = body ? JSON.parse(body) : null; } catch (e) { /* JSON 아님 */ }
            return { command, headers, json };
        });
}

class StompClient {
    constructor(memberId, role) {
        this.memberId = memberId;
        this.role = role;
        this.onMessage = () => {};
        this.probes = new Set();
        this.subSeq = 0;
    }

    connect() {
        return new Promise((resolve, reject) => {
            const timer = setTimeout(() => reject(new Error(`CONNECT 시간 초과 (member ${this.memberId})`)), 10000);
            this.ws = new WebSocket(WS_URL);
            this.ws.onopen = () => this.ws.send(frame('CONNECT', {
                'accept-version': '1.2', host: HOST,
                Authorization: `Bearer ${jwt(this.memberId, this.role)}`, 'heart-beat': '0,0',
            }));
            this.ws.onmessage = (e) => parseFrames(e.data).forEach((f) => {
                if (f.command === 'CONNECTED') { clearTimeout(timer); resolve(); return; }
                if (f.command === 'ERROR') { clearTimeout(timer); reject(new Error(`STOMP ERROR (member ${this.memberId})`)); return; }
                if (f.command !== 'MESSAGE') return;
                if (f.json && f.json.__probe) { this.probes.add(f.json.__probe); return; }
                this.onMessage(f.headers.destination, f.json);
            });
            this.ws.onerror = (e) => { clearTimeout(timer); reject(new Error(`WebSocket error: ${e.error}`)); };
        });
    }

    // simple broker는 SUBSCRIBE RECEIPT를 주지 않으므로 probe 메시지 왕복으로 구독 등록을 확인
    async subscribe(destination, probeDestination = destination) {
        this.ws.send(frame('SUBSCRIBE', { id: `sub-${this.subSeq++}`, destination }));
        const probeId = `${this.memberId}-${this.subSeq}-${Date.now()}`;
        for (let i = 0; i < 50; i++) {
            this.send(probeDestination, { __probe: probeId });
            await sleep(200);
            if (this.probes.has(probeId)) return;
        }
        throw new Error(`구독 확인 실패: ${destination} (member ${this.memberId})`);
    }

    send(destination, body) {
        this.ws.send(frame('SEND', { destination, 'content-type': 'application/json' }, JSON.stringify(body)));
    }

    close() { this.ws.close(); }
}

// ===== 구독자 =====

export async function subscriber() {
    const idx = exec.scenario.iterationInTest;   // 0 .. ROOMS*SUBSCRIBERS_PER_ROOM-1
    const r = Math.floor(idx / SUBSCRIBERS_PER_ROOM) + 1;
    const s = (idx % SUBSCRIBERS_PER_ROOM) + 1;

    const client = new StompClient(subscriberId(r, s), 'GUEST');
    client.onMessage = (destination, m) => {
        if (!m) return;
        if (m.event === 'UPDATE') broadcastReceived.add(1, { op: 'update' });
        else if (m.event === 'DELETE') broadcastReceived.add(1, { op: 'delete' });
    };
    await client.connect();
    await client.subscribe(`/topic/rooms/${roomId(r)}/answers`);
    subscriberReady.add(1);

    // 작성자가 끝난 뒤 GRACE_S까지 연결을 유지한 다음 종료
    const endAt = exec.scenario.startTime + (WARMUP_S + MEASURE_S + GRACE_S) * 1000;
    await sleep(Math.max(0, endAt - Date.now()));
    client.close();
}

// ===== 작성자 =====

class Waiters {
    constructor() { this.list = []; }

    // predicate가 참인 메시지가 오거나 timeout이 지나면 resolve
    wait(predicate, timeoutMs) {
        return new Promise((resolve) => {
            const w = { predicate, resolve: null };
            const timer = setTimeout(() => { this.remove(w); resolve(null); }, timeoutMs);
            w.resolve = (value) => { clearTimeout(timer); this.remove(w); resolve(value); };
            this.list.push(w);
        });
    }

    dispatch(kind, m) {
        this.list.slice().forEach((w) => { if (w.predicate(kind, m)) w.resolve({ kind, m }); });
    }

    remove(w) { this.list = this.list.filter((x) => x !== w); }
}

function normalizeCode(code) {
    if (KNOWN_CODES.includes(code)) return code;
    console.warn(`알 수 없는 에러 코드: ${code}`);
    return 'OTHER';
}

function fetchAnswer(r, q, answerId, token) {
    const res = http.get(`${HTTP_URL}/api/rooms/${roomId(r)}/questions/${q}/answers?size=100`,
        { headers: { Authorization: `Bearer ${token}` }, tags: { name: 'answers-list' } });
    if (res.status !== 200) return { ok: false, answer: null };
    return { ok: true, answer: res.json('answers').find((a) => a.answerId === answerId) || null };
}

// 작업을 보내고 (자기 작업의 브로드캐스트 | 에러 | 시간 초과) 중 먼저 오는 것을 기다린다
async function perform(client, waiters, op, destination, body, isOwnBroadcast) {
    opSent.add(1, { op });
    const outcome = waiters.wait((kind, m) => kind === 'error' || (kind === 'broadcast' && isOwnBroadcast(m)),
        OUTCOME_TIMEOUT_MS);
    client.send(destination, body);
    const result = await outcome;
    if (!result) {
        opTimeout.add(1, { op });
        return { broadcast: null, error: null };
    }
    if (result.kind === 'error') {
        requesterError.add(1, { op, code: normalizeCode(result.m && result.m.code) });
        return { broadcast: null, error: result.m };
    }
    opBroadcastSeen.add(1, { op });
    return { broadcast: result.m, error: null };
}

function recordApplied(op, applied, sawBroadcast) {
    if (!applied) return;
    opApplied.add(1, { op });
    broadcastExpected.add(SUBSCRIBERS_PER_ROOM, { op });
    if (!sawBroadcast) silentFailure.add(1, { op });
}

export async function writer() {
    const idx = exec.scenario.iterationInTest;   // 0 .. ROOMS*WRITERS_PER_ROOM-1
    const r = Math.floor(idx / WRITERS_PER_ROOM) + 1;
    const w = (idx % WRITERS_PER_ROOM) + 1;
    const me = writerId(r, w);
    const token = jwt(me, 'MEMBER');

    const client = new StompClient(me, 'MEMBER');
    const waiters = new Waiters();
    client.onMessage = (destination, m) => {
        if (!m) return;
        waiters.dispatch(destination.endsWith('/queue/errors') ? 'error' : 'broadcast', m);
    };
    await client.connect();
    await client.subscribe(`/topic/rooms/${roomId(r)}/answers`);
    await client.subscribe('/user/queue/errors', `/user/${me}/queue/errors`);

    const endAt = exec.scenario.startTime + MEASURE_S * 1000;
    let seq = 0;
    while (Date.now() < endAt) {
        const cycleStart = Date.now();
        const q = questionId(r, w, (seq % QUESTIONS_PER_WRITER) + 1);
        seq++;

        // 1) 생성 → 자기 CREATE 브로드캐스트에서 answerId 획득
        const createContent = `c-${VERSION}-${me}-${seq}`;
        const created = await perform(client, waiters, 'create', createPath(roomId(r), q),
            payload({ content: createContent }, me),
            (m) => m.event === 'CREATE' && m.content === createContent);
        if (!created.broadcast) {
            await sleep(Math.max(0, CYCLE_MS - (Date.now() - cycleStart)));
            continue;
        }
        const answerId = created.broadcast.answerId;

        // 2) 수정
        const updateContent = `u-${VERSION}-${me}-${seq}`;
        const updated = await perform(client, waiters, 'update', updatePath(roomId(r), answerId),
            payload({ content: updateContent }, me),
            (m) => m.event === 'UPDATE' && m.answerId === answerId);
        const afterUpdate = fetchAnswer(r, q, answerId, token);
        recordApplied('update', afterUpdate.ok && afterUpdate.answer && afterUpdate.answer.content === updateContent,
            !!updated.broadcast);

        // 3) 삭제
        const deleted = await perform(client, waiters, 'delete', deletePath(roomId(r), answerId),
            payload({}, me),
            (m) => m.event === 'DELETE' && m.answerId === answerId);
        const afterDelete = fetchAnswer(r, q, answerId, token);
        recordApplied('delete', afterDelete.ok && !afterDelete.answer, !!deleted.broadcast);

        await sleep(Math.max(0, CYCLE_MS - (Date.now() - cycleStart)));
    }
    client.close();
}

// ===== 결과 저장 =====

function count(data, name) {
    const m = data.metrics[name];
    return m ? m.values.count : 0;
}

export function handleSummary(data) {
    const byOp = {};
    OPS.forEach((op) => {
        const errors = {};
        KNOWN_CODES.forEach((code) => {
            const n = count(data, `requester_error{op:${op},code:${code}}`);
            if (n > 0) errors[code] = n;
        });
        const expected = count(data, `broadcast_expected{op:${op}}`);
        const received = count(data, `broadcast_received{op:${op}}`);
        byOp[op] = {
            sent: count(data, `op_sent{op:${op}}`),
            // 생성은 DB 반영을 따로 확인하지 않는다(CREATE 브로드캐스트로 answerId를 얻는 용도)
            applied: op === 'create' ? null : count(data, `op_applied{op:${op}}`),
            broadcast_seen_by_requester: count(data, `op_broadcast_seen{op:${op}}`),
            timeout: count(data, `op_timeout{op:${op}}`),
            silent_failure: count(data, `silent_failure{op:${op}}`),
            broadcast_expected: expected,
            broadcast_received: received,
            broadcast_receipt_rate: expected > 0 ? received / expected : null,
            requester_errors: errors,
        };
    });

    const expected = byOp.update.broadcast_expected + byOp.delete.broadcast_expected;
    const received = byOp.update.broadcast_received + byOp.delete.broadcast_received;
    const result = {
        scenario: 'C-broadcast',
        version: VERSION,
        run: RUN,
        finished_at: new Date().toISOString(),
        conditions: {
            rooms: ROOMS,
            writers_per_room: WRITERS_PER_ROOM,
            subscribers_per_room: SUBSCRIBERS_PER_ROOM,
            questions_per_writer: QUESTIONS_PER_WRITER,
            warmup_s: WARMUP_S, measure_s: MEASURE_S, grace_s: GRACE_S,
            cycle_ms: CYCLE_MS, outcome_timeout_ms: OUTCOME_TIMEOUT_MS,
            subscribers_ready: count(data, 'subscriber_ready'),
        },
        summary: {
            broadcast_receipt_rate: expected > 0 ? received / expected : null,
            broadcast_expected: expected,
            broadcast_received: received,
            applied_update_delete: byOp.update.applied + byOp.delete.applied,
            silent_failure: byOp.update.silent_failure + byOp.delete.silent_failure,
        },
        by_op: byOp,
        http_req_failed_rate: data.metrics.http_req_failed ? data.metrics.http_req_failed.values.rate : null,
        ws_sessions: count(data, 'ws_sessions'),
    };

    const file = `load-test/results/c-${VERSION}-run${RUN}.json`;
    return {
        [file]: JSON.stringify({ ...result, k6_metrics: data.metrics }, null, 2),
        stdout: `\n[${file}]\n${JSON.stringify(result, null, 2)}\n`,
    };
}
