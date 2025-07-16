package com.oronaminc.join.websocket.config;

import java.security.Principal;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import com.oronaminc.join.room.dto.RoomExitEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomWebSocketHandlerDecorator extends WebSocketHandlerDecorator {
    private static final String ATTRIBUTES_ROOM_ID_KEY = "roomId";

    // 연결된 세션 관리
    private final WebsocketSessionManager sessionManager;
    private final ApplicationEventPublisher publisher;

    public CustomWebSocketHandlerDecorator(WebSocketHandler delegate,
            WebsocketSessionManager sessionManager,
            ApplicationEventPublisher publisher
    ) {
        super(delegate);
        this.sessionManager = sessionManager;
        this.publisher = publisher;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 세션 연결되면 map에 저장
        sessionManager.registerSession(session);
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus)
            throws Exception {
        // 세션 연결 종료되면 map에서 제거
        exitRoomPublishEvent(session);
        sessionManager.removeSession(session.getId());
        super.afterConnectionClosed(session, closeStatus);
    }

    private void exitRoomPublishEvent(WebSocketSession session) {
        Principal principal = Objects.requireNonNull(session.getPrincipal());
        Long memberId = Long.valueOf(principal.getName());

        Object value = session.getAttributes().get(ATTRIBUTES_ROOM_ID_KEY);
        if (value == null) {
            return;
        }
        Long roomId = (Long) value;

        publisher.publishEvent(new RoomExitEvent(memberId, roomId));
    }

}
