# Answer WebSocket 부하 테스트 (k6)

`refactor/answer-websocket-auth` 브랜치의 Answer WebSocket 리팩토링 전후를 같은 조건에서 비교한 기록이다.

- **before**: `02da509` (리팩토링 전 dev)
- **after**: `35aa137` (`refactor/answer-websocket-auth`)

리팩토링의 주요 변경 중 측정 대상과 관련된 것은 다음 두 가지다.

1. 답변 수정·삭제 STOMP 경로에 `roomId` 추가 (`/app/answers/{answerId}/update` → `/app/rooms/{roomId}/answers/{answerId}/update`)
   - before는 경로에 `{roomId}`가 없는데 `@SendTo("/topic/rooms/{roomId}/answers")`를 사용해,
     DB 반영 후 브로드캐스트 목적지를 만들지 못하고 요청자에게 `SOCKET-2000`(SOCKET_RUNTIME_ERROR)을 보낸다.
2. 답변 수정·삭제 시 낙관적 락 충돌 재시도 (`AnswerFacade`, 50ms × 최대 10회, 소진 시 `ANSWER-007` CONFLICT_ANSWER)
   - 공감 수 변경도 Answer의 `@Version`을 올리므로, 공감이 몰리는 답변을 수정하면 버전 충돌이 난다.

## 구성

| 파일 | 내용 |
|---|---|
| `smoke.js` | VU 1개. 연결 → 구독 → 답변 생성·수정·삭제, 각 단계의 브로드캐스트·에러·DB 반영을 출력 |
| `scenario-c-broadcast.js` | 시나리오 C: 수정·삭제 브로드캐스트 수신률 |
| `scenario-a-conflict.js` | 시나리오 A: 공감 폭주 중 답변 수정 성공률 |
| `seed.sql` / `seed-c.sql` / `seed-a.sql` | 스모크 / C / A용 고정 ID 시드 (1000번대 / 2000번대 / 3000번대로 ID가 겹치지 않음) |
| `results/` | 본 측정 결과 JSON (`handleSummary` 출력, k6 원본 메트릭 포함) |

## 실행 방법

### 1. 로컬 설정 (저장소 밖에 둔다)

JWT 시크릿, DB 비밀번호가 들어가므로 저장소 밖(예: `../loadtest-config/application-loadtest.yml`)에 두고 커밋하지 않는다.
값은 모두 로컬 테스트 전용이다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/joinin_loadtest?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: <로컬 DB 비밀번호>
  jpa:
    hibernate:
      ddl-auto: create                  # 기동마다 스키마 재생성
    defer-datasource-initialization: true
  sql:
    init:
      mode: always                      # 기동마다 시드 재적재
      data-locations:
        - file:<저장소 경로>/load-test/seed.sql
        - file:<저장소 경로>/load-test/seed-c.sql
        - file:<저장소 경로>/load-test/seed-a.sql
      encoding: UTF-8
  security.oauth2.client: ...           # 카카오 설정은 더미 값 (src/test/resources/application.yml 참고)

jwt:
  secret: <로컬 전용 임의 값, 32바이트 이상>
  access-token-expiration: 3600000
  refresh-token-expiration: 1209600000

cloud.aws: ...                          # 더미 값
```

### 2. 서버 기동 (before / after를 한 번에 하나씩)

```bash
# before: worktree로 따로 빌드해 8081
git worktree add --detach ../OronaminC_before 02da509
(cd ../OronaminC_before && ./gradlew bootJar -x test)
java -jar ../OronaminC_before/build/libs/join-0.1.jar --server.port=8081 \
  --spring.config.additional-location=file:../loadtest-config/application-loadtest.yml

# after: 현재 브랜치를 8082
./gradlew bootJar -x test
java -jar build/libs/join-0.1.jar --server.port=8082 \
  --spring.config.additional-location=file:../loadtest-config/application-loadtest.yml
```

측정 중에는 서버를 하나만 띄운다. k6, 서버, DB가 같은 PC의 CPU를 나눠 쓰기 때문이다.

### 3. k6 실행

k6는 서버와 같은 방식(HS256, `sub`=memberId, `nickname`, `role`)으로 JWT를 직접 서명한다. 로컬 yml의 `jwt.secret`을 `JWT_SECRET`으로 넘긴다.

```bash
SECRET=<로컬 yml의 jwt.secret>
k6 run -e VERSION=before -e PORT=8081 -e JWT_SECRET=$SECRET load-test/smoke.js
k6 run -e VERSION=after  -e PORT=8082 -e JWT_SECRET=$SECRET -e RUN=1 load-test/scenario-c-broadcast.js
k6 run -e VERSION=after  -e PORT=8082 -e JWT_SECRET=$SECRET -e EMOJI_VUS=30 -e RUN=1 load-test/scenario-a-conflict.js
```

- `VERSION=before|after`로 버전 차이(경로, payload의 `memberId` 포함 여부)를 분기한다.
- 연결 경로는 SockJS 엔드포인트의 순수 WebSocket 경로 `ws://localhost:{port}/ws/websocket`이다.
- 결과는 `load-test/results/`에 JSON으로 저장된다 (C: `c-{version}-run{N}.json`, A: `a-{version}-vu{N}-run{M}.json`).

## 측정 환경

| 항목 | 값 |
|---|---|
| DB | MySQL 8.4.9 (로컬, InnoDB, REPEATABLE-READ) |
| 서버 | Spring Boot 3.5.3, OpenJDK 21.0.6, STOMP simple broker, 스레드 풀·커넥션 풀 기본값 |
| 부하 도구 | k6 v2.2.0 (`k6/websockets`), STOMP 1.2 프레임 직접 구성 |
| PC | AMD Ryzen 5 5600 (6C/12T), RAM 16GB, NVMe SSD, Windows 11 Pro |
| 배치 | k6, 서버, MySQL을 모두 같은 PC에서 실행 |
| 실행 방식 | 서버는 한 번에 하나, before → after를 번갈아 실행, 매 회 스키마·시드 재생성 |

## 스모크 (H2, MySQL 각 1회)

| 항목 | before | after |
|---|---|---|
| `/ws/websocket` 연결 | 성공 | 성공 |
| 생성: CREATE 브로드캐스트 | 수신 | 수신 |
| 수정·삭제: 브로드캐스트 | **0건** | 수신 |
| 수정·삭제: 요청자 에러 큐 | **`SOCKET-2000`** | 없음 |
| 수정·삭제: DB 반영 (REST 확인) | 반영됨 | 반영됨 |

before는 수정·삭제가 DB에 반영되는데도 요청자는 실패 코드를 받고 구독자에게는 아무것도 전달되지 않는다.
서버 로그상 컨트롤러는 끝까지 실행되었고(return 직전 로그 출력) 그 뒤 브로드캐스트 대신 에러가 전송되어, 반환값 처리(`@SendTo`) 단계의 실패로 판단했다.
(예외 메시지 자체는 `RuntimeException` 핸들러가 로그 없이 처리해 확인하지 못했다.)

## 시나리오 C: 수정·삭제 브로드캐스트 수신률

### 설계

- 방 5개 × (작성자 TEAM 2명 + 구독자 GUEST 20명) = VU 110 / WebSocket 110개
- 작성자: 1초 주기로 생성 → 수정 → 삭제 반복. 작성자마다 질문 10개를 돌려 써서 답변 생성 속도 제한(방·회원·질문당 10초 5회)에 걸리지 않게 했다.
- 수정·삭제마다 REST로 DB 반영 여부를 확인하고, **반영된 작업만** 기대 수신 건수에 넣는다.
- 타임라인: 구독 워밍업 10초 → 측정 60초 → 수신 대기 5초. 결과 대기 제한 3초.
- 지표
  - `broadcast_receipt_rate` = 구독자 수신 수 ÷ (DB 반영된 수정·삭제 수 × 방 구독자 수 20)
  - `silent_failure` = DB에는 반영됐는데 요청자가 브로드캐스트를 받지 못한 작업 수
  - 요청자 에러 코드 분포

### 결과 (MySQL, 버전별 3회)

| 버전 | 회차 | 수신률 | 수신 / 기대 | DB 반영 수정+삭제 | silent_failure | 요청자 에러 |
|---|---|---|---|---|---|---|
| before | 1 | 0.00% | 0 / 24,000 | 1,200 (600+600) | 1,200 | SOCKET-2000 × 1,200 |
| before | 2 | 0.00% | 0 / 24,000 | 1,200 (600+600) | 1,200 | SOCKET-2000 × 1,200 |
| before | 3 | 0.00% | 0 / 24,000 | 1,200 (600+600) | 1,200 | SOCKET-2000 × 1,200 |
| **before 평균** | | **0.00%** | 0 / 72,000 (합계) | **1,200** | **1,200** | SOCKET-2000 100% |
| after | 1 | 100.00% | 24,000 / 24,000 | 1,200 (600+600) | 0 | 없음 |
| after | 2 | 100.00% | 24,000 / 24,000 | 1,200 (600+600) | 0 | 없음 |
| after | 3 | 100.00% | 24,000 / 24,000 | 1,200 (600+600) | 0 | 없음 |
| **after 평균** | | **100.00%** | 72,000 / 72,000 (합계) | **1,200** | **0** | 없음 |

6회 모두 시간 초과 0건, HTTP 실패 0%, 구독 완료 100/100이었다.

### 해석

- 두 버전 모두 수정·삭제 요청은 100% DB에 반영됐다. 차이는 반영된 변경이 구독자에게 전달되는지 여부뿐이다.
- before는 반영된 1,200건 모두가 silent failure였다. DB는 바뀌었는데 요청자는 실패 코드를 받고, 같은 방 구독자는 아무것도 받지 못했다.
- 회차 간 편차가 없는 것은 부하에 따른 확률적 실패가 아니라 경로 버그에 의한 결정적 실패이기 때문이다.

## 시나리오 A: 공감 폭주 중 답변 수정 성공률

### 설계

- 방 1개, 답변 2개(수정·폭주 대상 300001 / probe 전용 300002), 작성자 1명, 공감 회원 최대 100명
- **emoji_storm**: 공감 VU 30 / 50 / 100명이 답변 300001에 create/delete를 번갈아 전송한다.
  450ms 간격(회원당 초당 약 2.2회, 속도 제한 초당 3회 이내)이고 결과를 기다리지 않는다.
- **author_update**: 작성자 1명이 폭주 시작 10초 뒤부터 60초 동안 답변 300001 수정을 순차로 보낸다.
  결과(자기 UPDATE 브로드캐스트, 에러, 5초 초과 중 하나)를 받으면 REST로 DB 반영을 확인하고 다음 수정으로 넘어간다. 수정 사이에는 최소 200ms 간격을 둔다.
- **emoji_probe**: 1명이 폭주와 행 경합이 없는 답변 300002에 500ms 간격으로 공감을 보내고, 전송부터 브로드캐스트 수신까지의 지연을 잰다.
  재시도 sleep이 메시지 처리 스레드를 붙잡아 다른 요청이 밀리는지 보는 **부작용 지표**다. 같은 VU가 공감 토픽을 구독해 폭주 대상의 공감 브로드캐스트 수(= 반영된 공감 수)도 센다.
- 수정 성공 판정은 두 버전 모두 **REST로 DB 반영 여부를 확인**하는 방식으로 통일했다. before는 반영돼도 `SOCKET-2000`이 오기 때문이다.
- 타임라인: 연결 8초 → 폭주만 10초 → 폭주와 수정 동시 60초 → 대기 3초
- 단계(30 → 50 → 100)마다 before → after를 번갈아 3회씩, 총 18회 실행했다.

### 결과: 답변 수정 (3회 평균)

| 공감 VU | 버전 | 수정 성공률 | 반영 / 전송 (회당) | 요청자 응답 분포 | 수정 지연 p50 / p95 / p99 (ms) |
|---|---|---|---|---|---|
| 30 | before | **72.8%** | 218.3 / 300 | SOCKET-2000 100% | 5.0 / 6.4 / 7.7 |
| 30 | after | **100.0%** | 300 / 300 | 정상 브로드캐스트 100% | 5.5 / 122.1 / 149.4 |
| 50 | before | **62.8%** | 188.3 / 300 | SOCKET-2000 100% | 5.0 / 6.0 / 7.0 |
| 50 | after | **100.0%** | 299.3 / 299.3 | 정상 브로드캐스트 100% | 5.8 / 131.0 / 193.2 |
| 100 | before | **74.8%** | 224.3 / 300 | SOCKET-2000 100% | 5.7 / 21.7 / 29.0 |
| 100 | after | **99.7%** | 289.7 / 290.7 | 브로드캐스트 99.7%, ANSWER-007 0.3% | 14.7 / 221.1 / 362.9 |

### 결과: 공감 폭주와 부작용 지표 (3회 평균)

| 공감 VU | 버전 | 공감 전송 / 반영 (회당) | 반영률 | 공감 에러 (전송 대비) | 공감 처리 지연(probe) p50 / p95 / p99 (ms) |
|---|---|---|---|---|---|
| 30 | before | 4,667 / 4,543 | 97.3% | EMOJI-001 0.5%, EMOJI-002 1.5%, EMOJI-004 0.7% | 6.0 / 9.3 / 24.5 |
| 30 | after | 4,663 / 4,524 | 97.0% | EMOJI-001 0.5%, EMOJI-002 1.7%, EMOJI-004 0.8% | 6.0 / 9.0 / 12.6 |
| 50 | before | 7,780 / 6,476 | 83.2% | EMOJI-001 4.4%, EMOJI-002 10.3%, EMOJI-004 2.1% | 6.0 / 9.0 / 139.0 |
| 50 | after | 7,775 / 6,440 | 82.8% | EMOJI-001 4.7%, EMOJI-002 10.6%, EMOJI-004 1.9% | 6.0 / 9.3 / 129.7 |
| 100 | before | 15,507 / 9,796 | 63.2% | EMOJI-001 11.1%, EMOJI-002 21.2%, EMOJI-004 4.4%, EMOJI-003 0.2% | 8.0 / 594.4 / 1,129.2 |
| 100 | after | 15,500 / 9,757 | 63.0% | EMOJI-001 11.1%, EMOJI-002 20.9%, EMOJI-004 4.9%, EMOJI-003 0.1% | 8.2 / 621.9 / 1,096.7 |

에러 코드: EMOJI-001 공감 재시도(`EmojiFacade`) 소진 / EMOJI-002 삭제할 공감 없음 / EMOJI-003 속도 제한 / EMOJI-004 이미 공감함

### 해석

- **재시도는 수정 성공률을 올렸다.**
  - before는 공감 VU 수와 관계없이 수정의 약 25~37%가 낙관적 락 충돌로 반영되지 않았고, 요청자는 성공과 실패 모두 `SOCKET-2000`을 받아 결과를 구분할 수 없었다.
  - after는 30·50 VU에서 100%, 100 VU에서 99.7%가 반영됐다. 나머지 0.3%(3회 합계 3건)는 재시도 10회를 소진한 경우로, `CONFLICT_ANSWER`라는 명확한 코드로 응답했다.
- **대가는 수정 지연이다.**
  - after의 p95는 약 120~220ms, p99는 약 150~360ms다.
  - before의 p95(6~22ms)가 더 빠르지만, 성공이든 실패든 `SOCKET-2000`이 즉시 돌아온 값이라 같은 기준의 비교는 아니다.
  - 100 VU에서는 지연이 수정 간격 200ms를 넘는 경우가 생겨 after의 수정 전송 수가 약간 적다(290.7 대 300).
- **공감 쪽 부작용은 측정되지 않았다.**
  - 경합 없는 답변으로 잰 공감 처리 지연, 공감 반영률, 처리량(초당 약 65 / 92 / 140건), 에러 분포가 모든 단계에서 before와 after가 사실상 같다.
  - 이번 조건에서는 작성자가 1명이고 순차로 보내서, 수정 재시도가 동시에 붙잡는 메시지 처리 스레드가 최대 1개였기 때문으로 본다.
- **예상과 달랐던 점**: before의 수정 성공률이 VU 수에 비례해 떨어지지 않았다(72.8% → 62.8% → 74.8%).
  공감 처리량이 초당 약 140건에서 포화되면서 수정과 충돌하는 빈도가 오히려 줄었을 가능성이 있으나, 검증하지 않은 가설이다.

## 이번 범위 밖: 다음 개선 대상

**공감 반영률 저하 (97% → 83% → 63%)**
공감 VU가 50명 이상이 되면 두 버전 모두 공감 쪽이 나빠진다.

- 같은 답변에 대한 공감 반영률이 떨어진다.
- `EMOJI-001`(재시도 소진)이 늘어난다.
- 경합 없는 요청의 공감 처리 지연 p95가 9ms에서 약 600ms로 늘어난다.

`EmojiFacade`의 낙관적 락 재시도와 `Thread.sleep`이 한 행에 몰리는 쓰기를 감당하지 못하는 것으로 보인다. 이번 리팩토링(답변 WebSocket 인증·경로·수정 재시도)의 범위 밖이며, 공감 카운트 처리 방식 개선의 다음 대상으로 남긴다.

`EMOJI-002`/`EMOJI-004`는 같은 세션이 보낸 create/delete가 서버에서 병렬로 처리되며 순서가 바뀐 결과로 보인다.
단, 클라이언트가 결과를 기다리지 않고 450ms 간격으로 번갈아 보내는 부하 설계의 영향도 섞여 있어, 이 비율을 실제 사용자 오류율로 읽으면 안 된다.

## 한계

- **처리량·한계 성능 지표가 아니다.**
  - C는 초당 수정·삭제 약 20건, 구독자 전송 약 400건 수준의 가벼운 부하로, 정합성(알림 전달 여부) 개선의 근거다.
  - A도 한 답변에 대한 경합 상황에서의 성공률을 본 것이며, 서버 전체 처리량을 잰 것이 아니다.
- **A의 작성자는 1명이다.** 작성자가 여럿이거나 동시 수정이 많은 환경에서 재시도 sleep이 스레드 풀에 주는 영향은 측정하지 않았다.
- **A의 수정 간 최소 200ms 간격은 원래 설계에 없던 조건이다.** 결과를 받는 즉시 다음 수정을 보내면 before는 실패 응답이 빨라 초당 약 109건, after는 약 29건을 보내 공감 쪽 경합 조건이 버전마다 달라졌다. 이 차이를 없애려고 본 측정 전에 두 버전의 수정 부하를 초당 5건으로 맞췄다.
- **A의 100 VU 2회(before run1, after run3)는 공감 VU 1명이 연결 거부로 빠진 채 진행됐다.** 원인은 `connectex: actively refused`로, 동시 연결이 몰리며 Windows 백로그에 걸린 것으로 보인다. 공감 전송 수가 약 1% 적고, 평균에 그대로 포함했다. 나머지 16회는 전원 정상 연결, HTTP 실패는 18회 모두 0%였다.
- **공감 에러의 작업 귀속은 일부 근사치다.** 에러 프레임에 어떤 작업의 실패인지 정보가 없어서, EMOJI-002/004는 코드로, EMOJI-001은 마지막으로 보낸 작업으로 분류했다.
- **k6, 서버, MySQL을 한 PC에서 실행했다.** 절대 수치는 환경에 따라 달라지므로 버전 간 상대 비교로만 읽어야 한다.
- **회차는 조건당 3회다.** p99처럼 표본이 적은 꼬리 지표는 회차 간 편차가 있다(각 JSON 참고).
- **시나리오 B(정상 부하에서 생성 → 수신 지연)는 수행하지 않았다.**
