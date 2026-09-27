// 시나리오 A: 공감 폭주 중 답변 수정 성공률
//
// 실행 예:
//   k6 run -e VERSION=after -e PORT=8082 -e JWT_SECRET=<로컬 yml의 jwt.secret> -e EMOJI_VUS=30 -e RUN=1 load-test/scenario-a-conflict.js
//
// 구성 (load-test/seed-a.sql):
//   - emoji_storm   : 공감 회원 EMOJI_VUS명이 답변 300001에 공감 create/delete를 번갈아 보낸다.
//                     (속도 제한: 회원·대상당 1초 3회 → EMOJI_INTERVAL_MS 간격으로 초당 약 2.2회)
//   - author_update : 작성자 1명이 폭주 시작 PRE_S초 뒤부터 답변 300001 수정을 순차로 보낸다.
//                     결과(자기 UPDATE 브로드캐스트 | 에러 | UPDATE_TIMEOUT_MS 초과)를 받으면 REST로 DB 반영을 확인하고 다음 수정.
//                     단, 수정 간 최소 UPDATE_MIN_INTERVAL_MS 간격을 둬 두 버전의 수정 부하를 같게 맞춘다.
//   - emoji_probe   : 1명이 폭주와 행 경합이 없는 답변 300002에 공감을 번갈아 보내고 send→브로드캐스트 지연을 잰다.
//                     (메시지 처리 스레드가 재시도 sleep에 묶여 다른 요청이 밀리는지 보는 부작용 지표)
//                     동시에 방 공감 토픽을 구독해 폭주 대상(300001)의 공감 브로드캐스트 수(=반영된 공감 수)를 센다.
//
// 수정 성공 판정은 두 버전 모두 REST로 DB 반영 여부를 확인하는 방식으로 통일한다.
// (before는 수정이 반영돼도 @SendTo 경로 문제로 SOCKET-2000이 오기 때문)

import { WebSocket } from 'k6/websockets';
import { setTimeout, clearTimeout } from 'k6/timers';
import { Counter, Rate, Trend } from 'k6/metrics';
import exec from 'k6/execution';
import http from 'k6/http';
import crypto from 'k6/crypto';
import encoding from 'k6/encoding';

const VERSION = __ENV.VERSION;
const HOST = __ENV.HOST || 'localhost';
const PORT = __ENV.PORT;
const JWT_SECRET = __ENV.JWT_SECRET;
const RUN = __ENV.RUN || '0';
const EMOJI_VUS = Number(__ENV.EMOJI_VUS || 30);

if (!['before', 'after'].includes(VERSION) || !PORT || !JWT_SECRET) {
    throw new Error('VERSION(before|after), PORT, JWT_SECRET 환경변수가 필요합니다.');
}
if (EMOJI_VUS < 1 || EMOJI_VUS > 100) {
    throw new Error('EMOJI_VUS는 1~100 (seed-a.sql 공감 회원 수)');
}

const WS_URL = `ws://${HOST}:${PORT}/ws/websocket`;
const HTTP_URL = `http://${HOST}:${PORT}`;

// ===== 측정 조건 (seed-a.sql 과 일치해야 함) =====

const ROOM_ID = 3001;
const QUESTION_ID = 300001;
const TARGET_ANSWER_ID = 300001;   // 폭주 대상 = 수정 대상
const PROBE_ANSWER_ID = 300002;    // probe 전용
const AUTHOR_ID = 30001;
const PROBE_ID = 30002;
const emojiMemberId = (i) => 30100 + i;   // i = 1..100

const CONNECT_S = 8;                 // 전원 연결·구독 완료 대기
const PRE_S = 10;                    // 폭주만 먼저 진행하는 시간
const MEASURE_S = Number(__ENV.MEASURE_S || 60);   // 폭주 + 수정 동시 진행 시간
const GRACE_S = 3;
const EMOJI_INTERVAL_MS = 450;
const PROBE_INTERVAL_MS = 500;
const UPDATE_TIMEOUT_MS = 5000;
// 수정 간 최소 간격. 결과가 빨리 오는 버전이 더 많은 수정을 보내 공감 쪽 경합이 달라지지 않도록 두 버전의 수정 부하를 맞춘다.
const UPDATE_MIN_INTERVAL_MS = 200;
const PROBE_TIMEOUT_MS = 5000;

const STORM_S = PRE_S + MEASURE_S;

const OUTCOMES = ['broadcast', 'timeout'];
const KNOWN_CODES = [
    'SOCKET-1001', 'SOCKET-1002', 'SOCKET-1003', 'SOCKET-2000', 'SOCKET-3000',
    'ANSWER-001', 'ANSWER-002', 'ANSWER-003', 'ANSWER-004', 'ANSWER-005', 'ANSWER-006', 'ANSWER-007',
    'EMOJI-001', 'EMOJI-002', 'EMOJI-003', 'EMOJI-004',
    'AUTH-001', 'AUTH-002', 'PARTICIPANT-001', 'OTHER',
];
const EMOJI_OPS = ['create', 'delete'];

export const options = {
    setupTimeout: '30s',
    summaryTrendStats: ['avg', 'min', 'med', 'p(95)', 'p(99)', 'max', 'count'],
    scenarios: {
        emoji_storm: {
            executor: 'per-vu-iterations', exec: 'emojiStorm',
            vus: EMOJI_VUS, iterations: 1, maxDuration: `${CONNECT_S + STORM_S + GRACE_S + 30}s`,
        },
        emoji_probe: {
            executor: 'per-vu-iterations', exec: 'emojiProbe',
            vus: 1, iterations: 1, maxDuration: `${CONNECT_S + STORM_S + GRACE_S + 30}s`,
        },
        author_update: {
            executor: 'per-vu-iterations', exec: 'authorUpdate',
            vus: 1, iterations: 1, maxDuration: `${CONNECT_S + STORM_S + GRACE_S + 30}s`,
        },
    },
    thresholds: buildTagThresholds(),
};

// ===== 메트릭 =====

const updateSent = new Counter('update_sent');
const updateSuccess = new Rate('update_success_rate');        // DB 반영 기준
const updateOutcome = new Counter('update_outcome');           // {outcome: broadcast | timeout | <에러코드>}
const updateLatency = new Trend('update_latency', true);       // send → 결과(브로드캐스트/에러) 수신
const updateLatencyApplied = new Trend('update_latency_applied', true);   // DB 반영된 수정만

const emojiSent = new Counter('emoji_sent');                   // {op}
const emojiError = new Counter('emoji_error');                 // {op, code}
const emojiStormApplied = new Counter('emoji_storm_applied');  // {op} 폭주 대상 공감 브로드캐스트 수 (probe가 관측)

const probeLatency = new Trend('emoji_probe_latency', true);   // probe 공감 send → 브로드캐스트
const probeSent = new Counter('emoji_probe_sent');
const probeTimeout = new Counter('emoji_probe_timeout');
const probeError = new Counter('emoji_probe_error');           // {code}

function buildTagThresholds() {
    const t = {};
    [...OUTCOMES, ...KNOWN_CODES].forEach((o) => { t[`update_outcome{outcome:${o}}`] = ['count>=0']; });
    EMOJI_OPS.forEach((op) => {
        t[`emoji_sent{op:${op}}`] = ['count>=0'];
        t[`emoji_storm_applied{op:${op}}`] = ['count>=0'];
        KNOWN_CODES.forEach((code) => { t[`emoji_error{op:${op},code:${code}}`] = ['count>=0']; });
    });
    KNOWN_CODES.forEach((code) => { t[`emoji_probe_error{code:${code}}`] = ['count>=0']; });
    return t;
}

function normalizeCode(code) {
    if (KNOWN_CODES.includes(code)) return code;
    console.warn(`알 수 없는 에러 코드: ${code}`);
    return 'OTHER';
}

// ===== 버전별 경로·payload 분기 =====

const isBefore = VERSION === 'before';
const payload = (body, memberId) => (isBefore ? { ...body, memberId } : body);
const updatePath = (r, a) => (isBefore ? `/app/answers/${a}/update` : `/app/rooms/${r}/answers/${a}/update`);
const emojiPath = (r, op) => `/app/rooms/${r}/emojis/${op}`;

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
const sleepUntil = (t) => sleep(Math.max(0, t - Date.now()));

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

    subscribeErrors() {
        return this.subscribe('/user/queue/errors', `/user/${this.memberId}/queue/errors`);
    }

    send(destination, body) {
        this.ws.send(frame('SEND', { destination, 'content-type': 'application/json' }, JSON.stringify(body)));
    }

    close() { this.ws.close(); }
}

class Waiters {
    constructor() { this.list = []; }

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

const isErrorQueue = (destination) => destination.endsWith('/queue/errors');

// ===== 타임라인 (모든 시나리오가 setup 시각을 기준으로 맞춘다) =====

export function setup() {
    const t0 = Date.now();
    return { stormStart: t0 + CONNECT_S * 1000 };
}

// ===== emoji_storm =====

export async function emojiStorm(data) {
    const i = exec.scenario.iterationInTest + 1;   // 1..EMOJI_VUS
    const me = emojiMemberId(i);
    const client = new StompClient(me, 'GUEST');

    let has = false;          // 시드에는 공감이 없으므로 create부터 시작
    let lastOp = null;
    client.onMessage = (destination, m) => {
        if (!isErrorQueue(destination) || !m) return;
        const code = normalizeCode(m.code);
        // 에러 프레임에는 어떤 작업의 실패인지 없으므로, 작업이 확정되는 코드는 코드로, 나머지는 마지막 작업으로 귀속
        // (같은 세션의 메시지도 서버에서 병렬 처리되어 순서가 바뀔 수 있으므로 lastOp 귀속은 근사치)
        const op = code === 'EMOJI-004' ? 'create' : code === 'EMOJI-002' ? 'delete' : (lastOp || 'unknown');
        emojiError.add(1, { op, code });
        // 에러 의미에 맞게 상태를 되돌려 create/delete 교대가 어긋나지 않게 한다
        if (code === 'EMOJI-004') has = true;            // 이미 존재
        else if (code === 'EMOJI-002') has = false;      // 존재하지 않음
        else if (lastOp) has = lastOp === 'delete';      // 그 밖의 실패: 마지막 작업이 반영되지 않은 것으로 간주
    };
    await client.connect();
    await client.subscribeErrors();

    const stormEnd = data.stormStart + STORM_S * 1000;
    // 전원이 같은 순간에 몰리지 않도록 시작 시각을 간격 안에서 분산
    await sleepUntil(data.stormStart + Math.floor(Math.random() * EMOJI_INTERVAL_MS));
    let next = Date.now();
    while (Date.now() < stormEnd) {
        const op = has ? 'delete' : 'create';
        lastOp = op;
        has = !has;
        emojiSent.add(1, { op });
        client.send(emojiPath(ROOM_ID, op), payload({ targetType: 'ANSWER', targetId: TARGET_ANSWER_ID }, me));
        next += EMOJI_INTERVAL_MS;
        await sleepUntil(next);
    }
    await sleep(GRACE_S * 1000);
    client.close();
}

// ===== emoji_probe =====

export async function emojiProbe(data) {
    const client = new StompClient(PROBE_ID, 'GUEST');
    const waiters = new Waiters();
    client.onMessage = (destination, m) => {
        if (!m) return;
        if (isErrorQueue(destination)) { waiters.dispatch('error', m); return; }
        if (m.targetId === TARGET_ANSWER_ID && (m.event === 'CREATE' || m.event === 'DELETE')) {
            emojiStormApplied.add(1, { op: m.event.toLowerCase() });
        }
        waiters.dispatch('broadcast', m);
    };
    await client.connect();
    await client.subscribe(`/topic/rooms/${ROOM_ID}/emojis`);
    await client.subscribeErrors();

    const stormEnd = data.stormStart + STORM_S * 1000;
    await sleepUntil(data.stormStart);
    let has = false;
    while (Date.now() < stormEnd) {
        const started = Date.now();
        const op = has ? 'delete' : 'create';
        const event = op.toUpperCase();
        probeSent.add(1);
        const outcome = waiters.wait((kind, m) => kind === 'error'
            || (m.targetId === PROBE_ANSWER_ID && m.event === event), PROBE_TIMEOUT_MS);
        client.send(emojiPath(ROOM_ID, op), payload({ targetType: 'ANSWER', targetId: PROBE_ANSWER_ID }, PROBE_ID));
        const result = await outcome;
        if (!result) {
            probeTimeout.add(1);
        } else if (result.kind === 'error') {
            const code = normalizeCode(result.m.code);
            probeError.add(1, { code });
            if (code === 'EMOJI-004') has = true;
            else if (code === 'EMOJI-002') has = false;
        } else {
            probeLatency.add(Date.now() - started);
            has = !has;
        }
        await sleepUntil(started + PROBE_INTERVAL_MS);
    }
    await sleep(GRACE_S * 1000);
    client.close();
}

// ===== author_update =====

function fetchContent(token) {
    const res = http.get(`${HTTP_URL}/api/rooms/${ROOM_ID}/questions/${QUESTION_ID}/answers?size=10`,
        { headers: { Authorization: `Bearer ${token}` }, tags: { name: 'answers-list' } });
    if (res.status !== 200) return null;
    const answer = res.json('answers').find((a) => a.answerId === TARGET_ANSWER_ID);
    return answer ? answer.content : null;
}

export async function authorUpdate(data) {
    const token = jwt(AUTHOR_ID, 'MEMBER');
    const client = new StompClient(AUTHOR_ID, 'MEMBER');
    const waiters = new Waiters();
    client.onMessage = (destination, m) => {
        if (!m) return;
        waiters.dispatch(isErrorQueue(destination) ? 'error' : 'broadcast', m);
    };
    await client.connect();
    await client.subscribe(`/topic/rooms/${ROOM_ID}/answers`);
    await client.subscribeErrors();

    const stormEnd = data.stormStart + STORM_S * 1000;
    await sleepUntil(data.stormStart + PRE_S * 1000);
    let seq = 0;
    while (Date.now() < stormEnd) {
        seq++;
        const content = `a-${VERSION}-${EMOJI_VUS}-${seq}`;
        const outcome = waiters.wait((kind, m) => kind === 'error'
            || (m.event === 'UPDATE' && m.answerId === TARGET_ANSWER_ID && m.content === content), UPDATE_TIMEOUT_MS);
        const started = Date.now();
        updateSent.add(1);
        client.send(updatePath(ROOM_ID, TARGET_ANSWER_ID), payload({ content }, AUTHOR_ID));
        const result = await outcome;
        const elapsed = Date.now() - started;

        let tag;
        if (!result) tag = 'timeout';
        else if (result.kind === 'error') tag = normalizeCode(result.m.code);
        else tag = 'broadcast';
        updateOutcome.add(1, { outcome: tag });
        if (result) updateLatency.add(elapsed);

        const applied = fetchContent(token) === content;
        updateSuccess.add(applied);
        if (applied && result) updateLatencyApplied.add(elapsed);

        await sleepUntil(started + UPDATE_MIN_INTERVAL_MS);
    }
    client.close();
}

// ===== 결과 저장 =====

function metric(data, name) { return data.metrics[name] ? data.metrics[name].values : null; }
function count(data, name) { const v = metric(data, name); return v ? v.count : 0; }
function trend(data, name) {
    const v = metric(data, name);
    if (!v) return null;
    return { count: v.count, avg: v.avg, p50: v.med, p95: v['p(95)'], p99: v['p(99)'], max: v.max };
}
function nonZero(data, names) {
    const out = {};
    names.forEach(([key, name]) => { const n = count(data, name); if (n > 0) out[key] = n; });
    return out;
}

export function handleSummary(data) {
    const successRate = metric(data, 'update_success_rate');
    const emoji = {};
    EMOJI_OPS.forEach((op) => {
        const sent = count(data, `emoji_sent{op:${op}}`);
        const applied = count(data, `emoji_storm_applied{op:${op}}`);
        emoji[op] = {
            sent,
            applied_broadcast: applied,
            applied_rate: sent > 0 ? applied / sent : null,
            errors: nonZero(data, KNOWN_CODES.map((c) => [c, `emoji_error{op:${op},code:${c}}`])),
        };
    });
    const emojiSentTotal = emoji.create.sent + emoji.delete.sent;
    const emojiAppliedTotal = emoji.create.applied_broadcast + emoji.delete.applied_broadcast;

    const result = {
        scenario: 'A-conflict',
        version: VERSION,
        emoji_vus: EMOJI_VUS,
        run: RUN,
        finished_at: new Date().toISOString(),
        conditions: {
            connect_s: CONNECT_S, pre_s: PRE_S, measure_s: MEASURE_S, grace_s: GRACE_S,
            emoji_interval_ms: EMOJI_INTERVAL_MS, probe_interval_ms: PROBE_INTERVAL_MS,
            update_timeout_ms: UPDATE_TIMEOUT_MS, update_min_interval_ms: UPDATE_MIN_INTERVAL_MS,
        },
        update: {
            sent: count(data, 'update_sent'),
            applied: successRate ? successRate.passes : 0,
            success_rate: successRate ? successRate.rate : null,
            outcomes: nonZero(data, [...OUTCOMES, ...KNOWN_CODES].map((o) => [o, `update_outcome{outcome:${o}}`])),
            latency_ms: trend(data, 'update_latency'),
            latency_applied_ms: trend(data, 'update_latency_applied'),
        },
        emoji_storm: {
            sent: emojiSentTotal,
            applied_broadcast: emojiAppliedTotal,
            applied_rate: emojiSentTotal > 0 ? emojiAppliedTotal / emojiSentTotal : null,
            applied_per_s: emojiAppliedTotal / STORM_S,
            by_op: emoji,
        },
        emoji_probe: {
            sent: count(data, 'emoji_probe_sent'),
            timeout: count(data, 'emoji_probe_timeout'),
            errors: nonZero(data, KNOWN_CODES.map((c) => [c, `emoji_probe_error{code:${c}}`])),
            latency_ms: trend(data, 'emoji_probe_latency'),
        },
        http_req_failed_rate: metric(data, 'http_req_failed') ? metric(data, 'http_req_failed').rate : null,
        ws_sessions: count(data, 'ws_sessions'),
    };

    const file = `load-test/results/a-${VERSION}-vu${EMOJI_VUS}-run${RUN}.json`;
    return {
        [file]: JSON.stringify({ ...result, k6_metrics: data.metrics }, null, 2),
        stdout: `\n[${file}]\n${JSON.stringify(result, null, 2)}\n`,
    };
}
