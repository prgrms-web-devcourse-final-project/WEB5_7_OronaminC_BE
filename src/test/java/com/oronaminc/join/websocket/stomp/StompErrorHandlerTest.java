package com.oronaminc.join.websocket.stomp;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;

/**
 * StompErrorHandler.findErrorException()의 cause 체인 탐색 로직 검증.
 * ex 자체가 ErrorException인 경우와, MessageDeliveryException 등으로 래핑된 경우
 * 모두 sendErrorMessage()로 처리되는지, 그리고 순환 참조에서도 무한 루프 없이
 * 기본 처리로 위임되는지를 확인한다.
 */
@DisplayName("StompErrorHandler cause 체인 탐색 검증")
class StompErrorHandlerTest {

    private final StompErrorHandler handler = new StompErrorHandler(new ObjectMapper());

    @Test
    @DisplayName("ex 자체가 ErrorException이면 sendErrorMessage로 처리된 응답을 반환한다")
    void directErrorException_isHandled() {
        // given
        ErrorException ex = new ErrorException(ErrorCode.UNAUTHORIZED_MEMBER);

        // when
        Message<byte[]> result = handler.handleClientMessageProcessingError(dummyClientMessage(), ex);

        // then
        assertErrorMessageFor(result, ErrorCode.UNAUTHORIZED_MEMBER);
    }

    @Test
    @DisplayName("ErrorException이 MessageDeliveryException의 cause로 래핑되어도 sendErrorMessage로 처리된다")
    void wrappedInMessageDeliveryException_isHandled() {
        // given
        ErrorException cause = new ErrorException(ErrorCode.STOMP_INVALID_DESTINATION);
        MessageDeliveryException wrapped =
            new MessageDeliveryException(dummyClientMessage(), "delivery failed", cause);

        // when
        Message<byte[]> result = handler.handleClientMessageProcessingError(dummyClientMessage(), wrapped);

        // then
        assertErrorMessageFor(result, ErrorCode.STOMP_INVALID_DESTINATION);
    }

    @Test
    @DisplayName("cause 체인 어디에도 ErrorException이 없으면 기본(super) 처리로 위임한다")
    void noErrorExceptionInChain_fallsBackToSuper() {
        // given
        RuntimeException plainException = new RuntimeException("일반 예외");

        // when
        Message<byte[]> result =
            handler.handleClientMessageProcessingError(dummyClientMessage(), plainException);

        // then: 기본 처리 결과는 커스텀 목적지(/user/queue/errors)를 갖지 않는다
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertThat(accessor.getDestination()).isNotEqualTo("/user/queue/errors");
    }

    @Test
    @DisplayName("cause 체인이 순환 참조여도 무한 루프 없이 기본 처리로 위임한다")
    void circularCauseChain_doesNotLoopForever() {
        // given: A -> B -> A 형태의 순환 cause 체인
        RuntimeException a = new RuntimeException("A");
        RuntimeException b = new RuntimeException("B");
        a.initCause(b);
        b.initCause(a);

        // when
        Message<byte[]> result = handler.handleClientMessageProcessingError(dummyClientMessage(), a);

        // then: 무한 루프에 빠지지 않고(테스트가 종료됨) 기본 처리로 위임되어 반환된다
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertThat(accessor.getDestination()).isNotEqualTo("/user/queue/errors");
    }

    private void assertErrorMessageFor(Message<byte[]> result, ErrorCode expectedErrorCode) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertThat(accessor.getCommand()).isEqualTo(StompCommand.ERROR);
        assertThat(accessor.getDestination()).isEqualTo("/user/queue/errors");

        String json = new String(result.getPayload());
        assertThat(json).contains(expectedErrorCode.getCode());
    }

    private Message<byte[]> dummyClientMessage() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
