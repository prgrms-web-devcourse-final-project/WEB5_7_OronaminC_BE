// Answer WebSocket 스모크 테스트 (VU 1개, 1회)
//
// 실행 예:
//   k6 run -e VERSION=after  -e PORT=8082 -e JWT_SECRET=<로컬 yml의 jwt.secret> load-test/smoke.js
//   k6 run -e VERSION=before -e PORT=8081 -e JWT_SECRET=<로컬 yml의 jwt.secret> load-test/smoke.js
//
// 흐름: CONNECT → SUBSCRIBE(방 토픽 / 에러 큐) → 답변 생성 → 수정 → 삭제
// 각 단계 후 일정 시간 동안 받은 브로드캐스트·에러를 모두 출력하고, 수정·삭제는 REST로 DB 반영 여부도 확인한다.
// 데이터는 load-test/seed.sql 의 고정 ID를 사용한다.

import { WebSocket } from 'k6/websockets';
import { setTimeout } from 'k6/timers';
import { check } from 'k6';
import http from 'k6/http';
import crypto from 'k6/crypto';
import encoding from 'k6/encoding';

const VERSION = __ENV.VERSION;
const HOST = __ENV.HOST || 'localhost';
const PORT = __ENV.PORT;
const JWT_SECRET = __ENV.JWT_SECRET;
const WAIT_MS = Number(__ENV.WAIT_MS || 3000);

if (!['before', 'after'].includes(VERSION) || !PORT || !JWT_SECRET) {
    throw new Error('VERSION(before|after), PORT, JWT_SECRET 환경변수가 필요합니다.');
}

const WS_URL = `ws://${HOST}:${PORT}/ws/websocket`;
const HTTP_URL = `http://${HOST}:${PORT}`;

// seed.sql 고정 데이터
const ROOM_ID = 1001;
const QUESTION_ID = 1001;
const WRITER = { id: 1002, nickname: 'team', role: 'MEMBER' };       // TEAM → 답변 작성 권한
const SUBSCRIBER = { id: 1004, nickname: 'guests', role: 'GUEST' };  // GUEST 구독자

export const options = { vus: 1, iterations: 1 };

// ===== 버전별 경로·payload 분기 =====

const isBefore = VERSION === 'before';

function payload(body, memberId) {
    return isBefore ? { ...body, memberId } : body;
}

function createPath(roomId, questionId) {
    return `/app/rooms/${roomId}/question/${questionId}/answers/create`;
}

function updatePath(roomId, answerId) {
    return isBefore ? `/app/answers/${answerId}/update` : `/app/rooms/${roomId}/answers/${answerId}/update`;
}

function deletePath(roomId, answerId) {
    return isBefore ? `/app/answers/${answerId}/delete` : `/app/rooms/${roomId}/answers/${answerId}/delete`;
}

// ===== JWT (서버 JwtTokenProvider와 같은 형식: HS256, sub=memberId, nickname, role) =====

function jwt(member) {
    const now = Math.floor(Date.now() / 1000);
    const header = encoding.b64encode(JSON.stringify({ alg: 'HS256', typ: 'JWT' }), 'rawurl');
    const body = encoding.b64encode(JSON.stringify({
        sub: String(member.id),
        nickname: member.nickname,
        role: member.role,
        iat: now,
        exp: now + 3600,
    }), 'rawurl');
    const signature = crypto.hmac('sha256', JWT_SECRET, `${header}.${body}`, 'base64rawurl');
    return `${header}.${body}.${signature}`;
}

// ===== STOMP =====

function frame(command, headers, body = '') {
    const lines = Object.entries(headers).map(([k, v]) => `${k}:${v}`);
    return `${command}\n${lines.join('\n')}\n\n${body}\0`;
}

function parseFrames(data) {
    return data.split('\0')
        .map((raw) => raw.replace(/^\n+/, ''))   // heart-beat(개행) 제거
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
            return { command, headers, body, json };
        });
}

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

class StompClient {
    constructor(name, member) {
        this.name = name;
        this.member = member;
        this.frames = [];        // 수신한 모든 프레임 (probe 제외)
        this.probes = new Set();
        this.subSeq = 0;
    }

    connect() {
        return new Promise((resolve, reject) => {
            const timer = setTimeout(() => reject(new Error(`[${this.name}] CONNECT 시간 초과`)), 5000);
            this.ws = new WebSocket(WS_URL);
            this.ws.onopen = () => {
                console.log(`[${this.name}] WebSocket open: ${WS_URL}`);
                this.ws.send(frame('CONNECT', {
                    'accept-version': '1.2',
                    host: HOST,
                    Authorization: `Bearer ${jwt(this.member)}`,
                    'heart-beat': '0,0',
                }));
            };
            this.ws.onmessage = (e) => {
                parseFrames(e.data).forEach((f) => {
                    if (f.command === 'CONNECTED') {
                        clearTimeout(timer);
                        console.log(`[${this.name}] STOMP CONNECTED (memberId=${this.member.id})`);
                        resolve();
                        return;
                    }
                    if (f.command === 'ERROR') {
                        clearTimeout(timer);
                        console.log(`[${this.name}] STOMP ERROR: ${f.headers.message || ''} ${f.body}`);
                        reject(new Error(`[${this.name}] STOMP ERROR`));
                        return;
                    }
                    if (f.json && f.json.__probe) {
                        this.probes.add(f.json.__probe);
                        return;
                    }
                    this.frames.push(f);
                });
            };
            this.ws.onerror = (e) => {
                clearTimeout(timer);
                reject(new Error(`[${this.name}] WebSocket error: ${e.error}`));
            };
        });
    }

    // simple broker는 SUBSCRIBE RECEIPT를 주지 않으므로, probe 메시지가 되돌아올 때까지 기다려 구독 등록을 확인한다
    async subscribe(destination, probeDestination = destination) {
        this.ws.send(frame('SUBSCRIBE', { id: `sub-${this.subSeq++}`, destination }));
        const probeId = `${this.name}-${Date.now()}-${Math.random()}`;
        for (let i = 0; i < 25; i++) {
            this.send(probeDestination, { __probe: probeId });
            await sleep(200);
            if (this.probes.has(probeId)) {
                console.log(`[${this.name}] SUBSCRIBE 확인: ${destination}`);
                return;
            }
        }
        throw new Error(`[${this.name}] 구독 확인 실패: ${destination}`);
    }

    send(destination, body) {
        this.ws.send(frame('SEND', { destination, 'content-type': 'application/json' }, JSON.stringify(body)));
    }

    drain() {
        const received = this.frames;
        this.frames = [];
        return received;
    }

    close() {
        this.ws.close();
    }
}

// ===== REST (DB 반영 확인) =====

function findAnswer(answerId) {
    const res = http.get(`${HTTP_URL}/api/rooms/${ROOM_ID}/questions/${QUESTION_ID}/answers?size=100`, {
        headers: { Authorization: `Bearer ${jwt(WRITER)}` },
    });
    if (res.status !== 200) {
        console.log(`[REST] 답변 목록 조회 실패: ${res.status} ${res.body}`);
        return { ok: false, answer: null };
    }
    const answer = res.json('answers').find((a) => a.answerId === answerId) || null;
    return { ok: true, answer };
}

// ===== 시나리오 =====

function report(step, subscriber, writer) {
    const broadcasts = subscriber.drain().filter((f) => f.command === 'MESSAGE');
    const errors = writer.drain().filter((f) => f.command === 'MESSAGE');
    console.log(`--- [${step}] 구독자 브로드캐스트 ${broadcasts.length}건`);
    broadcasts.forEach((f) => console.log(`    ${f.headers.destination} ${f.body}`));
    console.log(`--- [${step}] 작성자 에러 큐 ${errors.length}건`);
    errors.forEach((f) => console.log(`    ${f.headers.destination} ${f.body}`));
    return { broadcasts: broadcasts.map((f) => f.json), errors: errors.map((f) => f.json) };
}

export default async function () {
    console.log(`===== VERSION=${VERSION} ${WS_URL} =====`);

    const subscriber = new StompClient('subscriber', SUBSCRIBER);
    const writer = new StompClient('writer', WRITER);

    await subscriber.connect();
    await writer.connect();
    check(null, { 'ws: /ws/websocket CONNECT 성공': () => true });

    await subscriber.subscribe(`/topic/rooms/${ROOM_ID}/answers`);
    await writer.subscribe('/user/queue/errors', `/user/${WRITER.id}/queue/errors`);

    // 1) 생성
    const createContent = `smoke-create-${Date.now()}`;
    writer.send(createPath(ROOM_ID, QUESTION_ID), payload({ content: createContent }, WRITER.id));
    await sleep(WAIT_MS);
    const created = report('CREATE', subscriber, writer);
    const createEvent = created.broadcasts.find((m) => m && m.event === 'CREATE' && m.content === createContent);
    check(createEvent, { 'create: CREATE 브로드캐스트 수신': (m) => !!m });
    if (!createEvent) {
        console.log('CREATE 브로드캐스트를 받지 못해 수정·삭제 단계를 진행할 수 없습니다.');
        subscriber.close();
        writer.close();
        return;
    }
    const answerId = createEvent.answerId;
    console.log(`생성된 answerId=${answerId}`);

    // 2) 수정
    const updateContent = `smoke-update-${Date.now()}`;
    writer.send(updatePath(ROOM_ID, answerId), payload({ content: updateContent }, WRITER.id));
    await sleep(WAIT_MS);
    const updated = report('UPDATE', subscriber, writer);
    const afterUpdate = findAnswer(answerId);
    console.log(`[REST] 수정 후 DB content = ${afterUpdate.answer ? afterUpdate.answer.content : '(없음)'}`);
    check(updated, {
        'update: UPDATE 브로드캐스트 수신': (u) => u.broadcasts.some((m) => m && m.event === 'UPDATE' && m.answerId === answerId),
        'update: 에러 큐 비어 있음': (u) => u.errors.length === 0,
    });
    check(afterUpdate, { 'update: DB 반영됨(REST)': (r) => !!r.answer && r.answer.content === updateContent });

    // 3) 삭제
    writer.send(deletePath(ROOM_ID, answerId), payload({}, WRITER.id));
    await sleep(WAIT_MS);
    const deleted = report('DELETE', subscriber, writer);
    const afterDelete = findAnswer(answerId);
    console.log(`[REST] 삭제 후 DB 존재 여부 = ${afterDelete.answer ? '존재' : '없음'}`);
    check(deleted, {
        'delete: DELETE 브로드캐스트 수신': (d) => d.broadcasts.some((m) => m && m.event === 'DELETE' && m.answerId === answerId),
        'delete: 에러 큐 비어 있음': (d) => d.errors.length === 0,
    });
    check(afterDelete, { 'delete: DB 반영됨(REST)': (r) => r.ok && !r.answer });

    subscriber.close();
    writer.close();
}
