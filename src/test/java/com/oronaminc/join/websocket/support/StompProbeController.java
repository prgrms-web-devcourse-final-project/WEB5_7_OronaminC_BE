package com.oronaminc.join.websocket.support;

import java.util.Map;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

/**
 * STOMP 통합 테스트 전용 구독 준비 확인(probe) 엔드포인트.
 *
 * 클라이언트는 브로커 목적지(/topic, /queue, /user)로 직접 SEND할 수 없으므로,
 * /app/test/probe 로 probe를 보내면 서버가 요청한 목적지로 대신 전송한다.
 * 구독한 목적지에서 probe가 수신되면 브로커에 구독이 등록된 것으로 본다.
 *
 * {@code @TestComponent}라 컴포넌트 스캔 대상이 아니며, 필요한 테스트에서 {@code @Import}로 등록한다.
 */
@TestComponent
@Controller
@RequiredArgsConstructor
public class StompProbeController {

    public static final String PROBE_PATH = "/app/test/probe";
    public static final String PROBE_FIELD = "__probe";

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/test/probe")
    public void probe(@Payload ProbeRequest request) {
        messagingTemplate.convertAndSend(request.destination(), Map.of(PROBE_FIELD, request.probeId()));
    }

    public record ProbeRequest(String destination, String probeId) {
    }
}
