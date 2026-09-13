package com.oronaminc.join.websocket.config;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.token.JwtTokenProvider;
import com.oronaminc.join.member.token.TokenBody;
import com.oronaminc.join.websocket.stomp.StompPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String header = accessor.getFirstNativeHeader("Authorization");

            if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
                throw new ErrorException(ErrorCode.UNAUTHORIZED_MEMBER);
            }

            String token = header.substring(7);

            TokenBody body;
            try {
                body = jwtTokenProvider.parseClaims(token);
            } catch (Exception e) {
                throw new ErrorException(ErrorCode.UNAUTHORIZED_MEMBER);
            }

            StompPrincipal stompPrincipal = new StompPrincipal(String.valueOf(body.memberId()));
            accessor.setUser(stompPrincipal);
        }

        return message;
    }
}
