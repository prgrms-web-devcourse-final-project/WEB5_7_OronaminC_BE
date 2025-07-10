package com.oronaminc.join.websocket.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.global.exception.ErrorResponse;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Configuration
@RequiredArgsConstructor
public class StompErrorHandler extends StompSubProtocolErrorHandler {
// 어딘가에서 소켓 ErrorException이 터지면 이 핸들러가 잡아서 stomp 에러로 변환함


    private final ObjectMapper objectMapper;

    // 클라이언트 메시지 처리 중 오류가 발생했을 때 호출
    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage,
        Throwable ex) {

        if (ex instanceof ErrorException ee) {
            return sendErrorMessage(ee);
        }

        return super.handleClientMessageProcessingError(clientMessage, ex);
    }

    // 실제 stomp 에러를 클라이언트에게 전송
    private Message<byte[]> sendErrorMessage(ErrorException ee) {

        ErrorCode errorCode = ee.getErrorCode();

        // StompHeaderAccessor 헤더 정보를 담는 클래스
        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.ERROR);
        // 에러 메시지를 헤더에 포함
        headers.setMessage(ee.getMessage());

        try {
            String json = objectMapper.writeValueAsString(
                new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));

            return MessageBuilder.createMessage(json.getBytes(StandardCharsets.UTF_8),
                headers.getMessageHeaders());

        } catch (JsonProcessingException e) {
            return MessageBuilder.createMessage(
                errorCode.getMessage().getBytes(StandardCharsets.UTF_8),
                headers.getMessageHeaders());
        }


    }
}
