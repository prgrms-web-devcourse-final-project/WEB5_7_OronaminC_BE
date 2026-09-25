package com.oronaminc.join.websocket.api;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.member.token.JwtMemberInfo;
import com.oronaminc.join.member.token.JwtTokenProvider;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.question.dao.QuestionRepository;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.domain.RoomStatus;
import com.oronaminc.join.room.domain.RoomType;

/**
 * AnswerWebsocketController를 실제 서버(RANDOM_PORT)에 SockJS + STOMP로 연결해
 * 인증(CONNECT JWT) → 컨트롤러 → 브로드캐스트(@SendTo) / 에러 전송(@SendToUser) 흐름을 검증하는 통합 테스트.
 *
 * simple broker는 SUBSCRIBE에 RECEIPT를 보내지 않고 inbound 채널도 비동기라,
 * 구독 직후 바로 SEND하면 구독 등록보다 메시지 처리가 먼저 끝날 수 있다.
 * 그래서 구독한 목적지로 probe 메시지를 보내 실제로 수신될 때까지 기다린 뒤 시나리오를 진행한다.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        // 다른 컨텍스트와 H2 스키마를 공유하지 않도록 전용 in-memory DB 사용
        "spring.datasource.url=jdbc:h2:mem:answer-stomp-it;DB_CLOSE_DELAY=-1",
        "jwt.secret=answer-websocket-integration-test-secret-key-must-be-32-bytes-plus",
        "jwt.access-token-expiration=3600000",
        "jwt.refresh-token-expiration=1209600000"
    }
)
@ActiveProfiles("test")
@DisplayName("AnswerWebsocketController STOMP 통합 검증")
class AnswerWebsocketIntegrationTest {

    private static final long TIMEOUT_SECONDS = 10;
    private static final String PROBE_FIELD = "__probe";
    private static final String ERROR_QUEUE = "/user/queue/errors";
    private static final AtomicInteger SEQ = new AtomicInteger();

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    private WebSocketStompClient stompClient;
    private final List<StompSession> sessions = new ArrayList<>();

    // 방 A: 발표자 / 팀원 / 질문 작성자(게스트) / 구독자(게스트)
    private Room roomA;
    private Member presenter;
    private Member teamMember;
    private Member questionWriter;
    private Member subscriber;
    private Question questionA;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(
            new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);

        roomA = saveRoom();
        presenter = saveMember("발표자");
        teamMember = saveMember("팀원");
        questionWriter = saveMember("질문자");
        subscriber = saveMember("구독자");

        saveParticipant(roomA, presenter, ParticipantType.PRESENTER);
        saveParticipant(roomA, teamMember, ParticipantType.TEAM);
        saveParticipant(roomA, questionWriter, ParticipantType.GUEST);
        saveParticipant(roomA, subscriber, ParticipantType.GUEST);

        questionA = saveQuestion(roomA, questionWriter);
    }

    @AfterEach
    void tearDown() {
        sessions.stream().filter(StompSession::isConnected).forEach(StompSession::disconnect);
        stompClient.stop();
    }

    @Test
    @DisplayName("답변 생성 시 같은 방 구독자가 /topic/rooms/{roomId}/answers 로 CREATE 이벤트를 수신한다")
    void create_broadcastsToRoomSubscribers() throws Exception {
        // given
        Inbox roomTopic = subscribe(connect(subscriber), answersTopic(roomA));
        StompSession presenterSession = connect(presenter);
        Inbox errors = subscribeErrors(presenterSession, presenter);

        // when
        presenterSession.send(createPath(roomA, questionA), Map.of("content", "첫 답변"));

        // then
        JsonNode message = roomTopic.next(errors);
        assertThat(message.get("event").asText()).isEqualTo("CREATE");
        assertThat(message.get("questionId").asLong()).isEqualTo(questionA.getId());
        assertThat(message.get("content").asText()).isEqualTo("첫 답변");
        assertThat(message.get("writer").get("memberId").asLong()).isEqualTo(presenter.getId());

        Long answerId = message.get("answerId").asLong();
        assertThat(answerRepository.findById(answerId)).isPresent();
    }

    @Test
    @DisplayName("답변 수정 시 구독자가 UPDATE 이벤트를 수신한다")
    void update_broadcastsToRoomSubscribers() throws Exception {
        // given
        Answer answer = saveAnswer(questionA, presenter, "수정 전");
        Inbox roomTopic = subscribe(connect(subscriber), answersTopic(roomA));
        StompSession presenterSession = connect(presenter);
        Inbox errors = subscribeErrors(presenterSession, presenter);

        // when
        presenterSession.send(updatePath(roomA, answer), Map.of("content", "수정 후"));

        // then
        JsonNode message = roomTopic.next(errors);
        assertThat(message.get("event").asText()).isEqualTo("UPDATE");
        assertThat(message.get("answerId").asLong()).isEqualTo(answer.getId());
        assertThat(message.get("content").asText()).isEqualTo("수정 후");

        assertThat(answerRepository.findById(answer.getId()))
            .get().extracting(Answer::getContent).isEqualTo("수정 후");
    }

    @Test
    @DisplayName("답변 삭제 시 구독자가 DELETE 이벤트를 수신한다")
    void delete_broadcastsToRoomSubscribers() throws Exception {
        // given
        Answer answer = saveAnswer(questionA, presenter, "삭제될 답변");
        Inbox roomTopic = subscribe(connect(subscriber), answersTopic(roomA));
        StompSession presenterSession = connect(presenter);
        Inbox errors = subscribeErrors(presenterSession, presenter);

        // when
        presenterSession.send(deletePath(roomA, answer), Map.of());

        // then
        JsonNode message = roomTopic.next(errors);
        assertThat(message.get("event").asText()).isEqualTo("DELETE");
        assertThat(message.get("answerId").asLong()).isEqualTo(answer.getId());

        assertThat(answerRepository.findById(answer.getId())).isEmpty();
    }

    @Test
    @DisplayName("payload에 다른 회원의 memberId를 넣어도 답변 작성자는 JWT의 memberId로 저장된다")
    void create_ignoresForgedMemberIdInPayload() throws Exception {
        // given
        Inbox roomTopic = subscribe(connect(subscriber), answersTopic(roomA));
        StompSession presenterSession = connect(presenter);
        Inbox errors = subscribeErrors(presenterSession, presenter);

        // when: 발표자의 JWT로 연결한 세션에서 payload에 팀원의 memberId를 끼워 넣는다
        // (위조 대상이 답변 권한이 없으면 권한 검증에서 먼저 거절되어 작성자 검증까지 도달하지 못하므로 TEAM 멤버를 사용)
        presenterSession.send(createPath(roomA, questionA),
            Map.of("content", "위조 시도", "memberId", teamMember.getId()));

        // then
        JsonNode message = roomTopic.next(errors);
        assertThat(message.get("writer").get("memberId").asLong())
            .as("작성자는 JWT의 memberId여야 함")
            .isEqualTo(presenter.getId());

        Long answerId = message.get("answerId").asLong();
        Long savedWriterId = transactionTemplate.execute(status ->
            answerRepository.findById(answerId).orElseThrow().getMember().getId());
        assertThat(savedWriterId).as("작성자는 JWT의 memberId여야 함").isEqualTo(presenter.getId());
    }

    @Test
    @DisplayName("다른 방의 roomId로 수정하면 /user/queue/errors 로 NOT_FOUND_ANSWER를 받고 브로드캐스트되지 않는다")
    void update_withOtherRoomId_sendsNotFoundAndDoesNotBroadcast() throws Exception {
        // given: 발표자가 방 A에 작성한 답변, 발표자는 방 B의 발표자이기도 함
        Answer answer = saveAnswer(questionA, presenter, "원본");

        Room roomB = saveRoom();
        saveParticipant(roomB, presenter, ParticipantType.PRESENTER);
        saveParticipant(roomB, subscriber, ParticipantType.GUEST);
        Question questionB = saveQuestion(roomB, subscriber);

        StompSession subscriberSession = connect(subscriber);
        Inbox topicA = subscribe(subscriberSession, answersTopic(roomA));
        Inbox topicB = subscribe(subscriberSession, answersTopic(roomB));

        StompSession presenterSession = connect(presenter);
        Inbox errors = subscribeErrors(presenterSession, presenter);

        // when: 경로의 roomId만 방 B로 바꿔 방 A의 답변을 수정 시도
        presenterSession.send(updatePath(roomB, answer), Map.of("content", "다른 방에서 수정"));

        // then: 요청자에게 NOT_FOUND_ANSWER
        JsonNode error = errors.next();
        assertThat(error.get("code").asText()).isEqualTo(ErrorCode.NOT_FOUND_ANSWER.getCode());

        // then: 에러 수신 이후 각 방에 정상 답변(sentinel)을 생성해, sentinel보다 앞서 도착한 메시지가 없음을 확인
        presenterSession.send(createPath(roomB, questionB), Map.of("content", "sentinel-B"));
        assertThat(topicB.messagesBefore("sentinel-B")).isEmpty();

        presenterSession.send(createPath(roomA, questionA), Map.of("content", "sentinel-A"));
        assertThat(topicA.messagesBefore("sentinel-A")).isEmpty();

        assertThat(answerRepository.findById(answer.getId()))
            .get().extracting(Answer::getContent).isEqualTo("원본");
    }

    @Test
    @DisplayName("발표자가 다른 사람의 답변을 삭제하면 UNAUTHORIZED_DELETE_ANSWER를 받고 삭제·브로드캐스트되지 않는다")
    void delete_byPresenterNotWriter_isRejected() throws Exception {
        assertDeleteRejected(presenter);
    }

    @Test
    @DisplayName("질문 작성자가 다른 사람의 답변을 삭제하면 UNAUTHORIZED_DELETE_ANSWER를 받고 삭제·브로드캐스트되지 않는다")
    void delete_byQuestionWriterNotAnswerWriter_isRejected() throws Exception {
        assertDeleteRejected(questionWriter);
    }

    @Test
    @DisplayName("빈 content로 답변을 생성하면 /user/queue/errors 로 SOCKET_VALIDATION_ERROR를 받고 브로드캐스트되지 않는다")
    void create_withBlankContent_sendsValidationErrorAndDoesNotBroadcast() throws Exception {
        // given
        Inbox roomTopic = subscribe(connect(subscriber), answersTopic(roomA));
        StompSession presenterSession = connect(presenter);
        Inbox errors = subscribeErrors(presenterSession, presenter);

        // when
        presenterSession.send(createPath(roomA, questionA), Map.of("content", ""));

        // then
        JsonNode error = errors.next();
        assertThat(error.get("code").asText()).isEqualTo(ErrorCode.SOCKET_VALIDATION_ERROR.getCode());
        assertThat(error.get("message").asText()).isEqualTo("답변 내용을 입력해주시기 바랍니다.");

        presenterSession.send(createPath(roomA, questionA), Map.of("content", "sentinel"));
        assertThat(roomTopic.messagesBefore("sentinel")).isEmpty();
    }

    @Test
    @DisplayName("Authorization 헤더 없이 CONNECT하면 ERROR 프레임(UNAUTHORIZED_MEMBER)과 함께 연결이 거부된다")
    void connect_withoutJwt_isRejected() {
        // given
        BlockingQueue<StompHeaders> errorFrames = new LinkedBlockingQueue<>();
        BlockingQueue<String> errorBodies = new LinkedBlockingQueue<>();
        StompSessionHandlerAdapter handler = new StompSessionHandlerAdapter() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                errorFrames.add(headers);
                errorBodies.add(new String((byte[])payload, StandardCharsets.UTF_8));
            }
        };

        // when
        CompletableFuture<StompSession> future =
            stompClient.connectAsync(wsUrl(), new WebSocketHttpHeaders(), new StompHeaders(), handler);

        // then: CONNECTED를 받지 못하고 연결이 종료되어 future가 예외로 완료된다
        assertThatThrownBy(() -> future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
            .isInstanceOf(ExecutionException.class);

        // then: 서버는 StompErrorHandler를 통해 ERROR 프레임으로 인증 실패 사유를 내려준다
        assertThat(errorFrames).hasSize(1);
        assertThat(errorBodies.peek()).contains(ErrorCode.UNAUTHORIZED_MEMBER.getCode());
    }

    // ===== 시나리오 헬퍼 =====

    private void assertDeleteRejected(Member requester) throws Exception {
        // given: 팀원이 작성한 답변
        Answer answer = saveAnswer(questionA, teamMember, "팀원 답변");
        Inbox roomTopic = subscribe(connect(subscriber), answersTopic(roomA));

        StompSession requesterSession = connect(requester);
        Inbox errors = subscribeErrors(requesterSession, requester);

        // when
        requesterSession.send(deletePath(roomA, answer), Map.of());

        // then
        JsonNode error = errors.next();
        assertThat(error.get("code").asText()).isEqualTo(ErrorCode.UNAUTHORIZED_DELETE_ANSWER.getCode());

        StompSession teamSession = connect(teamMember);
        teamSession.send(createPath(roomA, questionA), Map.of("content", "sentinel"));
        assertThat(roomTopic.messagesBefore("sentinel")).isEmpty();

        assertThat(answerRepository.findById(answer.getId())).isPresent();
    }

    // ===== STOMP 헬퍼 =====

    private StompSession connect(Member member) throws Exception {
        String accessToken = jwtTokenProvider.generateTokenPair(
            new JwtMemberInfo(member.getId(), member.getNickname(), MemberType.MEMBER)).accessToken();

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + accessToken);

        StompSession session = stompClient
            .connectAsync(wsUrl(), new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {})
            .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        sessions.add(session);
        return session;
    }

    private Inbox subscribe(StompSession session, String destination) throws InterruptedException {
        return subscribe(session, destination, destination);
    }

    // /user/queue/errors 구독은 서버에서 세션별 큐로 변환되므로, probe는 /user/{memberId}/queue/errors 로 보낸다
    private Inbox subscribeErrors(StompSession session, Member member) throws InterruptedException {
        return subscribe(session, ERROR_QUEUE, "/user/" + member.getId() + "/queue/errors");
    }

    private Inbox subscribe(StompSession session, String destination, String probeDestination)
        throws InterruptedException {
        Inbox inbox = new Inbox();
        session.subscribe(destination, inbox);

        String probeId = UUID.randomUUID().toString();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
        while (true) {
            session.send(probeDestination, Map.of(PROBE_FIELD, probeId));
            if (inbox.awaitProbe(probeId, 200)) {
                return inbox;
            }
            if (System.nanoTime() > deadline) {
                throw new AssertionError("구독이 제한 시간 내에 등록되지 않았습니다: " + destination);
            }
        }
    }

    private String wsUrl() {
        return "http://localhost:" + port + "/ws";
    }

    private static String answersTopic(Room room) {
        return "/topic/rooms/" + room.getId() + "/answers";
    }

    private static String createPath(Room room, Question question) {
        return "/app/rooms/" + room.getId() + "/question/" + question.getId() + "/answers/create";
    }

    private static String updatePath(Room room, Answer answer) {
        return "/app/rooms/" + room.getId() + "/answers/" + answer.getId() + "/update";
    }

    private static String deletePath(Room room, Answer answer) {
        return "/app/rooms/" + room.getId() + "/answers/" + answer.getId() + "/delete";
    }

    /**
     * 구독 한 건의 수신함. probe 메시지는 준비 확인용으로만 쓰고 시나리오 메시지 큐에는 넣지 않는다.
     */
    private static final class Inbox implements StompFrameHandler {

        private final BlockingQueue<JsonNode> messages = new LinkedBlockingQueue<>();
        private final BlockingQueue<String> probes = new LinkedBlockingQueue<>();

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return JsonNode.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            JsonNode node = (JsonNode)payload;
            if (node.has(PROBE_FIELD)) {
                probes.add(node.get(PROBE_FIELD).asText());
                return;
            }
            messages.add(node);
        }

        boolean awaitProbe(String probeId, long timeoutMillis) throws InterruptedException {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
            long remaining;
            while ((remaining = deadline - System.nanoTime()) > 0) {
                String received = probes.poll(remaining, TimeUnit.NANOSECONDS);
                if (probeId.equals(received)) {
                    return true;
                }
            }
            return false;
        }

        JsonNode next() throws InterruptedException {
            return next(null);
        }

        // 요청자의 에러 큐를 함께 넘기면, 제한 시간 내에 수신하지 못했을 때 그동안 받은 에러를 실패 메시지에 담는다
        JsonNode next(Inbox requesterErrors) throws InterruptedException {
            JsonNode message = messages.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (message == null) {
                throw new AssertionError(TIMEOUT_SECONDS + "초 내에 메시지를 수신하지 못했습니다."
                    + describeErrors(requesterErrors));
            }
            return message;
        }

        private static String describeErrors(Inbox errors) {
            if (errors == null) {
                return "";
            }
            List<JsonNode> received = new ArrayList<>();
            errors.messages.drainTo(received);
            if (received.isEmpty()) {
                return " (요청자 /user/queue/errors: 수신 없음)";
            }
            return " (요청자 /user/queue/errors: " + received.stream()
                .map(e -> e.path("code").asText() + " " + e.path("message").asText())
                .collect(Collectors.joining(", ")) + ")";
        }

        // content가 sentinelContent인 메시지가 올 때까지 기다리며, 그 전에 도착한 메시지들을 반환한다
        List<JsonNode> messagesBefore(String sentinelContent) throws InterruptedException {
            List<JsonNode> before = new ArrayList<>();
            while (true) {
                JsonNode message = next();
                if (message.has("content") && sentinelContent.equals(message.get("content").asText())) {
                    return before;
                }
                before.add(message);
            }
        }
    }

    // ===== 데이터 준비 =====

    private Member saveMember(String nickname) {
        int seq = SEQ.incrementAndGet();
        return memberRepository.save(Member.builder()
            .email("stomp-it-" + seq + "@test.com")
            .nickname(nickname + seq)
            .memberType(MemberType.MEMBER)
            .build());
    }

    private Room saveRoom() {
        return roomRepository.save(Room.builder()
            .title("STOMP 통합 테스트 방")
            .description("설명")
            .secretCode("IT" + SEQ.incrementAndGet())
            .roomStatus(RoomStatus.STARTED)
            .roomType(RoomType.PUBLIC)
            .emojiCount(0L)
            .participantLimit(100)
            .endedAt(LocalDateTime.now().plusDays(1))
            .build());
    }

    private void saveParticipant(Room room, Member member, ParticipantType type) {
        participantRepository.save(Participant.builder()
            .room(room)
            .member(member)
            .participantType(type)
            .build());
    }

    private Question saveQuestion(Room room, Member writer) {
        return questionRepository.save(Question.builder()
            .room(room)
            .member(writer)
            .content("질문")
            .emojiCount(0L)
            .build());
    }

    private Answer saveAnswer(Question question, Member writer, String content) {
        return answerRepository.save(Answer.create(question, writer, content));
    }
}
