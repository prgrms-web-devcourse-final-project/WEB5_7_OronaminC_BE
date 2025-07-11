package com.oronaminc.join.websocket.api;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.global.exception.ErrorResponse;
import com.oronaminc.join.websocket.config.CustomWebSocketHandlerDecorator;
import com.oronaminc.join.websocket.config.WebsocketSessionManager;
import java.io.IOException;
import java.net.SocketException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class WebSocketExceptionHandler {

    /*
    * @SendToUser("/queue/errors")
    * -> "/user/queue/errors" 를 구독한 유저에게만 에러 알림이 감.
    * => 프론트에서 connect 이후 해당 경로를 구독해줘야 서버에서 전송한 에러 메시지가 전달됨.
    * */

    private final WebsocketSessionManager sessionManager;

    @MessageExceptionHandler(SocketException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleSocketException(Message<?> message) throws IOException {
    // 세션 강제 종료 or 소켓 내부 오류

        removeSession(message);

        return new ErrorResponse(ErrorCode.SOCKET_ERROR);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public ErrorResponse handleCustomException(ErrorException e) {
    // 비즈니스 오류 (ex. 존재하지 않는 ~~에 접근)

        return new ErrorResponse(e.getErrorCode());
    }

    @MessageExceptionHandler(RuntimeException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleIllegalArgumentException() {
    // 처리되지 않은 오류

        return new ErrorResponse(ErrorCode.SOCKET_RUNTIME_ERROR);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        return new ErrorResponse(
            ErrorCode.SOCKET_VALIDATION_ERROR.getCode(),
            ex.getMessage()
        );
    }

    private void removeSession(Message<?> message) throws IOException {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        String sessionId = headerAccessor.getSessionId();

        log.info("종료된 sessionId = {}", sessionId);
        sessionManager.closeSession(sessionId);
    }
}
