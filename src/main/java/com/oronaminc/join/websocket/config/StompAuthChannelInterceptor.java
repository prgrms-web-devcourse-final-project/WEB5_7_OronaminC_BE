package com.oronaminc.join.websocket.config;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.token.JwtTokenProvider;
import com.oronaminc.join.member.token.TokenBody;
import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.websocket.stomp.StompPrincipal;
import com.oronaminc.join.websocket.stomp.StompPrincipalUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    // 발표방 토픽 /topic/rooms/{roomId}/... (roomId는 Long으로 파싱 가능한 길이까지만 매칭)
    private static final Pattern ROOM_TOPIC = Pattern.compile("^/topic/rooms/(\\d{1,18})(/.*)?$");
    private static final String APPLICATION_DESTINATION =
        WebSocketConfig.APPLICATION_DESTINATION_PREFIX + "/";
    // simple broker와 같은 AntPathMatcher 기준으로 와일드카드 구독 여부를 판단한다
    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtTokenProvider jwtTokenProvider;
    private final ParticipantService participantService;

    // preSend에서 던진 예외는 StompErrorHandler가 ERROR 프레임으로 변환하고, 해당 프레임은 브로커에 전달되지 않는다
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            authenticate(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            validateSubscribe(accessor);
        } else if (StompCommand.SEND.equals(command)) {
            validateSend(accessor);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
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

    // 와일드카드 구독은 여러 방의 토픽을 한 번에 받을 수 있으므로 거부하고,
    // 발표방 토픽은 해당 방 참가자만 구독할 수 있다
    private void validateSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        if (!StringUtils.hasText(destination) || PATH_MATCHER.isPattern(destination)) {
            throw new ErrorException(ErrorCode.STOMP_INVALID_DESTINATION);
        }

        Matcher matcher = ROOM_TOPIC.matcher(destination);
        if (!matcher.matches()) {
            return;
        }

        Long roomId = Long.valueOf(matcher.group(1));
        Long memberId = StompPrincipalUtil.getMemberId(accessor.getUser());
        participantService.validateParticipant(memberId, roomId);
    }

    // 클라이언트는 애플리케이션 목적지(/app/...)로만 SEND할 수 있다.
    // 브로커(/topic, /queue)나 유저 목적지(/user)로 직접 보내면 컨트롤러 검증 없이 구독자에게 전달되기 때문
    private void validateSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        if (destination == null || !destination.startsWith(APPLICATION_DESTINATION)) {
            throw new ErrorException(ErrorCode.STOMP_INVALID_DESTINATION);
        }
    }
}
