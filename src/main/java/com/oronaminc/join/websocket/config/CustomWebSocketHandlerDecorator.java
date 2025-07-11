package com.oronaminc.join.websocket.config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

public class CustomWebSocketHandlerDecorator extends WebSocketHandlerDecorator {
// 연결된 세션 관리

    private final WebsocketSessionManager sessionManager;

    public CustomWebSocketHandlerDecorator( WebSocketHandler delegate, WebsocketSessionManager sessionManager) {
        super(delegate);
        this.sessionManager = sessionManager;
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

        sessionManager.removeSession(session.getId());
        super.afterConnectionClosed(session, closeStatus);
    }

}
