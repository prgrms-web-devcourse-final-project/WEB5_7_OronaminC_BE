package com.oronaminc.join.websocket.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.member.token.JwtConfiguration;
import com.oronaminc.join.member.token.JwtMemberInfo;
import com.oronaminc.join.member.token.JwtTokenProvider;
import com.oronaminc.join.websocket.stomp.StompPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;

@DisplayName("StompAuthChannelInterceptor CONNECT 인증 검증")
class StompAuthChannelInterceptorTest {

    private static final String SECRET =
        "stomp-auth-channel-interceptor-test-secret-key-must-be-32-bytes-plus";

    private JwtTokenProvider jwtTokenProvider;
    private StompAuthChannelInterceptor interceptor;
    private MessageChannel channel;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
            new JwtConfiguration(SECRET, 3_600_000L, 1_209_600_000L));
        interceptor = new StompAuthChannelInterceptor(jwtTokenProvider);
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

    private Message<byte[]> connectFrameWithAuthorization(String authorizationHeader) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (authorizationHeader != null) {
            accessor.addNativeHeader("Authorization", authorizationHeader);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
