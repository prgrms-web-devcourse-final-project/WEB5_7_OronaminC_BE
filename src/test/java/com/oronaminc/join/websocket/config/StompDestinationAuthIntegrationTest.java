package com.oronaminc.join.websocket.config;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.member.token.JwtMemberInfo;
import com.oronaminc.join.member.token.JwtTokenProvider;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.domain.RoomStatus;
import com.oronaminc.join.room.domain.RoomType;
import com.oronaminc.join.websocket.support.StompProbeController;
import com.oronaminc.join.websocket.support.StompProbeController.ProbeRequest;

/**
 * StompAuthChannelInterceptor의 SUBSCRIBE / SEND 목적지 검증 회귀 테스트.
 *
 * - 발표방 토픽(/topic/rooms/{roomId}/...)은 해당 방 참가자만 구독할 수 있다.
 * - 와일드카드 구독은 거부된다.
 * - 클라이언트는 /app/... 로만 SEND할 수 있고, 브로커(/topic, /queue)나 유저(/user) 목적지로 직접 보낼 수 없다.
 *
 * 거부는 ERROR 프레임(StompErrorHandler)으로 내려가며, STOMP 규약에 따라 서버가 해당 연결을 닫는다.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:stomp-destination-auth-it;DB_CLOSE_DELAY=-1",
        "jwt.secret=stomp-destination-auth-integration-test-secret-key-must-be-32-bytes",
        "jwt.access-token-expiration=3600000",
        "jwt.refresh-token-expiration=1209600000"
    }
)
@ActiveProfiles("test")
@Import(StompProbeController.class)
@DisplayName("STOMP SUBSCRIBE / SEND 목적지 권한 검증")
class StompDestinationAuthIntegrationTest {

    private static final long TIMEOUT_SECONDS = 10;
    private static final String ERROR_QUEUE = "/user/queue/errors";
    private static final AtomicInteger SEQ = new AtomicInteger();

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    private WebSocketStompClient stompClient;
    private final List<StompSession> sessions = new ArrayList<>();

    // 방 A: 참가자 2명(질문 작성자, 구독자) / 외부인: 로그인만 했고 방 A에 참가하지 않은 회원
    private Room roomA;
    private Member writer;
    private Member subscriber;
    private Member outsider;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(
            new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);

        roomA = saveRoom();
        writer = saveMember("작성자");
        subscriber = saveMember("구독자");
        outsider = saveMember("외부인");

        saveParticipant(roomA, writer, ParticipantType.GUEST);
        saveParticipant(roomA, subscriber, ParticipantType.GUEST);
    }

    @AfterEach
    void tearDown() {
        sessions.stream().filter(StompSession::isConnected).forEach(StompSession::disconnect);
        stompClient.stop();
    }

    // ===== SUBSCRIBE =====

    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(strings = {"answers", "questions", "emojis", "join"})
    @DisplayName("참가하지 않은 방의 토픽을 구독하면 ERROR 프레임(NOT_FOUND_PARTICIPANT)을 받고 연결이 종료된다")
    void subscribe_roomTopicByNonParticipant_isRejected(String suffix) throws Exception {
        // given
        Client client = connect(outsider);

        // when
        client.session().subscribe(roomTopic(roomA, suffix), new Inbox());

        // then
        assertThat(client.nextError()).contains(ErrorCode.NOT_FOUND_PARTICIPANT.getCode());
        client.awaitDisconnected();
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"/topic/rooms/*/questions", "/topic/rooms/**", "/topic/**"})
    @DisplayName("와일드카드 목적지를 구독하면 ERROR 프레임(STOMP_INVALID_DESTINATION)을 받는다")
    void subscribe_wildcardDestination_isRejected(String destination) throws Exception {
        // given: 참가자라도 여러 방을 한 번에 구독할 수는 없다
        Client client = connect(subscriber);

        // when
        client.session().subscribe(destination, new Inbox());

        // then
        assertThat(client.nextError()).contains(ErrorCode.STOMP_INVALID_DESTINATION.getCode());
        client.awaitDisconnected();
    }

    @Test
    @DisplayName("참가한 방의 questions 토픽을 구독하면 /app 경로로 생성된 질문을 수신한다")
    void subscribe_roomTopicByParticipant_receivesQuestion() throws Exception {
        // given
        Inbox topic = subscribe(connect(subscriber), roomTopic(roomA, "questions"));
        Client writerClient = connect(writer);
        Inbox errors = subscribe(writerClient, ERROR_QUEUE, userErrorQueue(writer));

        // when
        writerClient.session().send(appPath(roomA, "questions/create"), Map.of("content", "방A 질문"));

        // then
        JsonNode message = topic.next(errors);
        assertThat(message.get("event").asText()).isEqualTo("CREATE");
        assertThat(message.get("content").asText()).isEqualTo("방A 질문");
    }

    @Test
    @DisplayName("참가한 방의 emojis 토픽을 구독하면 /app 경로로 생성된 공감을 수신한다")
    void subscribe_roomTopicByParticipant_receivesEmoji() throws Exception {
        // given
        Inbox topic = subscribe(connect(subscriber), roomTopic(roomA, "emojis"));
        Client writerClient = connect(writer);
        Inbox errors = subscribe(writerClient, ERROR_QUEUE, userErrorQueue(writer));

        // when
        writerClient.session().send(appPath(roomA, "emojis/create"),
            Map.of("targetType", "ROOM", "targetId", roomA.getId()));

        // then
        JsonNode message = topic.next(errors);
        assertThat(message.get("event").asText()).isEqualTo("CREATE");
        assertThat(message.get("targetId").asLong()).isEqualTo(roomA.getId());
    }

    @Test
    @DisplayName("roomId를 파싱할 수 없는 /topic/rooms/ 하위 목적지는 기존처럼 구독할 수 있다")
    void subscribe_unparsableRoomTopic_keepsExistingBehavior() throws Exception {
        // when & then: probe가 수신되면 구독이 브로커에 등록된 것
        subscribe(connect(outsider), "/topic/rooms/not-a-number/questions");
    }

    // ===== SEND =====

    @Test
    @DisplayName("/topic 으로 직접 SEND하면 ERROR 프레임(STOMP_INVALID_DESTINATION)을 받고 구독자에게 전달되지 않는다")
    void send_directlyToTopic_isRejectedAndNotBroadcast() throws Exception {
        // given
        String questionsTopic = roomTopic(roomA, "questions");
        Inbox topic = subscribe(connect(subscriber), questionsTopic);
        Client outsiderClient = connect(outsider);

        // when
        outsiderClient.session().send(questionsTopic, Map.of("event", "CREATE", "content", "주입한 가짜 질문"));

        // then: 보낸 쪽은 거부되고 연결이 종료된다
        assertThat(outsiderClient.nextError()).contains(ErrorCode.STOMP_INVALID_DESTINATION.getCode());
        outsiderClient.awaitDisconnected();

        // then: 정상 경로로 sentinel 질문을 만들어, 그보다 앞서 도착한 메시지가 없음을 확인
        connect(writer).session().send(appPath(roomA, "questions/create"), Map.of("content", "sentinel"));
        assertThat(topic.messagesBefore("sentinel")).isEmpty();
    }

    @Test
    @DisplayName("다른 회원의 /user/{memberId}/queue/errors 로 직접 SEND하면 거부되고 해당 회원에게 전달되지 않는다")
    void send_directlyToUserDestination_isRejectedAndNotDelivered() throws Exception {
        // given
        Client subscriberClient = connect(subscriber);
        Inbox subscriberErrors = subscribe(subscriberClient, ERROR_QUEUE, userErrorQueue(subscriber));
        Client outsiderClient = connect(outsider);

        // when
        outsiderClient.session().send(userErrorQueue(subscriber), Map.of("code", "FAKE", "message", "가짜 에러"));

        // then: 보낸 쪽은 거부된다
        assertThat(outsiderClient.nextError()).contains(ErrorCode.STOMP_INVALID_DESTINATION.getCode());

        // then: 구독자가 실제 에러(빈 질문)를 유발했을 때 처음 받는 에러가 서버가 보낸 검증 에러여야 한다
        subscriberClient.session().send(appPath(roomA, "questions/create"), Map.of("content", ""));
        JsonNode firstError = subscriberErrors.next(null);
        assertThat(firstError.get("code").asText()).isEqualTo(ErrorCode.SOCKET_VALIDATION_ERROR.getCode());
    }

    // ===== 헬퍼 =====

    private Client connect(Member member) throws Exception {
        String accessToken = jwtTokenProvider.generateTokenPair(
            new JwtMemberInfo(member.getId(), member.getNickname(), MemberType.MEMBER)).accessToken();

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + accessToken);

        BlockingQueue<String> errorFrames = new LinkedBlockingQueue<>();
        StompSessionHandlerAdapter handler = new StompSessionHandlerAdapter() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            // 구독에 속하지 않은 프레임(ERROR)은 세션 핸들러로 전달된다
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                errorFrames.add(new String((byte[])payload, StandardCharsets.UTF_8));
            }
        };

        StompSession session = stompClient
            .connectAsync(wsUrl(), new WebSocketHttpHeaders(), connectHeaders, handler)
            .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        sessions.add(session);
        return new Client(session, errorFrames);
    }

    private Inbox subscribe(Client client, String destination) throws InterruptedException {
        return subscribe(client, destination, destination);
    }

    // simple broker는 SUBSCRIBE에 RECEIPT를 보내지 않으므로, 서버가 구독 목적지로 보낸 probe가 수신될 때까지 기다린다
    private Inbox subscribe(Client client, String destination, String probeDestination)
        throws InterruptedException {
        Inbox inbox = new Inbox();
        client.session().subscribe(destination, inbox);

        String probeId = UUID.randomUUID().toString();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
        while (true) {
            client.session().send(StompProbeController.PROBE_PATH, new ProbeRequest(probeDestination, probeId));
            if (inbox.awaitProbe(probeId, 200)) {
                return inbox;
            }
            if (System.nanoTime() > deadline) {
                throw new AssertionError("구독이 제한 시간 내에 등록되지 않았습니다: " + destination
                    + " (ERROR 프레임: " + client.errorFrames() + ")");
            }
        }
    }

    private String wsUrl() {
        return "http://localhost:" + port + "/ws";
    }

    private static String roomTopic(Room room, String suffix) {
        return "/topic/rooms/" + room.getId() + "/" + suffix;
    }

    private static String appPath(Room room, String suffix) {
        return "/app/rooms/" + room.getId() + "/" + suffix;
    }

    // /user/queue/errors 구독은 서버에서 세션별 큐로 변환되므로, 서버 측 전송은 /user/{memberId}/queue/errors 로 한다
    private static String userErrorQueue(Member member) {
        return "/user/" + member.getId() + "/queue/errors";
    }

    private record Client(StompSession session, BlockingQueue<String> errorFrames) {

        String nextError() throws InterruptedException {
            String error = errorFrames.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (error == null) {
                throw new AssertionError(TIMEOUT_SECONDS + "초 내에 ERROR 프레임을 수신하지 못했습니다.");
            }
            return error;
        }

        void awaitDisconnected() throws InterruptedException {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
            while (session.isConnected()) {
                if (System.nanoTime() > deadline) {
                    throw new AssertionError("ERROR 프레임 이후 연결이 종료되지 않았습니다.");
                }
                Thread.sleep(50);
            }
        }
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
            if (node.has(StompProbeController.PROBE_FIELD)) {
                probes.add(node.get(StompProbeController.PROBE_FIELD).asText());
                return;
            }
            messages.add(node);
        }

        boolean awaitProbe(String probeId, long timeoutMillis) throws InterruptedException {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
            while (true) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    return false;
                }
                String received = probes.poll(remaining, TimeUnit.NANOSECONDS);
                if (probeId.equals(received)) {
                    return true;
                }
            }
        }

        // 요청자의 에러 큐를 함께 넘기면, 제한 시간 내에 수신하지 못했을 때 그동안 받은 에러를 실패 메시지에 담는다
        JsonNode next(Inbox requesterErrors) throws InterruptedException {
            JsonNode message = messages.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (message == null) {
                String errors = requesterErrors == null ? "" : " (요청자 에러: " + requesterErrors.messages + ")";
                throw new AssertionError(TIMEOUT_SECONDS + "초 내에 메시지를 수신하지 못했습니다." + errors);
            }
            return message;
        }

        // content가 sentinelContent인 메시지가 올 때까지 기다리며, 그 전에 도착한 메시지들을 반환한다
        List<JsonNode> messagesBefore(String sentinelContent) throws InterruptedException {
            List<JsonNode> before = new ArrayList<>();
            while (true) {
                JsonNode message = next(null);
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
            .email("stomp-auth-it-" + seq + "@test.com")
            .nickname(nickname + seq)
            .memberType(MemberType.MEMBER)
            .build());
    }

    private Room saveRoom() {
        return roomRepository.save(Room.builder()
            .title("STOMP 권한 테스트 방")
            .description("설명")
            .secretCode("AUTH" + SEQ.incrementAndGet())
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
}
