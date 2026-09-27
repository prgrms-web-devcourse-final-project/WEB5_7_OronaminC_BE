package com.oronaminc.join.websocket.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.member.token.JwtConfiguration;
import com.oronaminc.join.member.token.JwtMemberInfo;
import com.oronaminc.join.member.token.JwtTokenProvider;
import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.websocket.stomp.StompPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;

@DisplayName("StompAuthChannelInterceptor CONNECT 인증 및 SUBSCRIBE / SEND 목적지 검증")
class StompAuthChannelInterceptorTest {

    private static final String SECRET =
        "stomp-auth-channel-interceptor-test-secret-key-must-be-32-bytes-plus";

    private JwtTokenProvider jwtTokenProvider;
    private ParticipantService participantService;
    private StompAuthChannelInterceptor interceptor;
    private MessageChannel channel;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
            new JwtConfiguration(SECRET, 3_600_000L, 1_209_600_000L));
        participantService = mock(ParticipantService.class);
        interceptor = new StompAuthChannelInterceptor(jwtTokenProvider, participantService);
        channel = mock(MessageChannel.class);
    }

    @Test
    @DisplayName("유효한 JWT를 담은 CONNECT 프레임은 accessor.getUser()에 memberId 기반 StompPrincipal을 설정한다")
    void connectWithValidJwt_setsStompPrincipal() {
        // given
        String token = jwtTokenProvider.generateTokenPair(
            new JwtMemberInfo(7L, "유저", MemberType.MEMBER)).accessToken();
        Message<byte[]> connectFrame = connectFrameWithAuthorization("Bearer " + token);

        // when
        Message<?> result = interceptor.preSend(connectFrame, channel);

        // then
        StompHeaderAccessor resultAccessor =
            MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertThat(resultAccessor.getUser()).isInstanceOf(StompPrincipal.class);
        assertThat(resultAccessor.getUser().getName()).isEqualTo("7");
    }

    @Test
    @DisplayName("Authorization 헤더가 없는 CONNECT 프레임은 ErrorException(UNAUTHORIZED_MEMBER)을 던진다")
    void connectWithoutAuthorizationHeader_throwsUnauthorized() {
        // given
        Message<byte[]> connectFrame = connectFrameWithAuthorization(null);

        // when & then
        assertThatThrownBy(() -> interceptor.preSend(connectFrame, channel))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_MEMBER);
    }

    @Test
    @DisplayName("유효하지 않은 JWT를 담은 CONNECT 프레임은 ErrorException(UNAUTHORIZED_MEMBER)을 던진다")
    void connectWithInvalidJwt_throwsUnauthorized() {
        // given
        Message<byte[]> connectFrame = connectFrameWithAuthorization("Bearer invalid.jwt.token");

        // when & then
        assertThatThrownBy(() -> interceptor.preSend(connectFrame, channel))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_MEMBER);
    }

    @Test
    @DisplayName("발표방 토픽 SUBSCRIBE는 Principal의 memberId와 경로의 roomId로 참가자 검증을 한다")
    void subscribeRoomTopic_validatesParticipant() {
        // given
        Message<byte[]> frame = frame(StompCommand.SUBSCRIBE, "/topic/rooms/42/answers", "7");

        // when
        interceptor.preSend(frame, channel);

        // then
        verify(participantService).validateParticipant(7L, 42L);
    }

    @Test
    @DisplayName("참가자가 아니면 SUBSCRIBE는 참가자 검증 예외(NOT_FOUND_PARTICIPANT)로 거부된다")
    void subscribeRoomTopicByNonParticipant_throws() {
        // given
        doThrow(new ErrorException(ErrorCode.NOT_FOUND_PARTICIPANT))
            .when(participantService).validateParticipant(7L, 42L);
        Message<byte[]> frame = frame(StompCommand.SUBSCRIBE, "/topic/rooms/42/questions", "7");

        // when & then
        assertThatThrownBy(() -> interceptor.preSend(frame, channel))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_PARTICIPANT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/user/queue/errors", "/topic/rooms/not-a-number/answers", "/topic/notice"})
    @DisplayName("발표방 토픽 패턴이 아닌 SUBSCRIBE는 참가자 검증 없이 통과한다")
    void subscribeNonRoomTopic_passesWithoutValidation(String destination) {
        // given
        Message<byte[]> frame = frame(StompCommand.SUBSCRIBE, destination, "7");

        // when
        Message<?> result = interceptor.preSend(frame, channel);

        // then
        assertThat(result).isSameAs(frame);
        verifyNoInteractions(participantService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/topic/rooms/*/answers", "/topic/rooms/**", "/topic/rooms/{id}/answers", "/topic/room?/1"})
    @DisplayName("와일드카드 목적지 SUBSCRIBE는 STOMP_INVALID_DESTINATION으로 거부된다")
    void subscribeWildcardDestination_throws(String destination) {
        // given
        Message<byte[]> frame = frame(StompCommand.SUBSCRIBE, destination, "7");

        // when & then
        assertThatThrownBy(() -> interceptor.preSend(frame, channel))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STOMP_INVALID_DESTINATION);
        verifyNoInteractions(participantService);
    }

    @Test
    @DisplayName("애플리케이션 목적지(/app/...)로의 SEND는 통과한다")
    void sendToApplicationDestination_passes() {
        // given
        Message<byte[]> frame = frame(StompCommand.SEND, "/app/rooms/42/questions/create", "7");

        // when
        Message<?> result = interceptor.preSend(frame, channel);

        // then
        assertThat(result).isSameAs(frame);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/topic/rooms/42/questions", "/queue/errors", "/user/8/queue/errors", "/application/x"})
    @DisplayName("애플리케이션 목적지가 아닌 SEND는 STOMP_INVALID_DESTINATION으로 거부된다")
    void sendToNonApplicationDestination_throws(String destination) {
        // given
        Message<byte[]> frame = frame(StompCommand.SEND, destination, "7");

        // when & then
        assertThatThrownBy(() -> interceptor.preSend(frame, channel))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STOMP_INVALID_DESTINATION);
    }

    private Message<byte[]> frame(StompCommand command, String destination, String memberId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setDestination(destination);
        accessor.setUser(new StompPrincipal(memberId));
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Message<byte[]> connectFrameWithAuthorization(String authorizationHeader) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (authorizationHeader != null) {
            accessor.addNativeHeader("Authorization", authorizationHeader);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
