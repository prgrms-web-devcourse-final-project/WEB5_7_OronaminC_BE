package com.oronaminc.join.websocket.config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

@Component
public class WebsocketSessionManager {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void registerSession(WebSocketSession session) {
        sessions.put(session.getId(), session);

    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    public void closeSession(String sessionId) throws IOException {
        WebSocketSession session = sessions.remove(sessionId);

        if (session != null && session.isOpen()) {
            session.close(CloseStatus.PROTOCOL_ERROR);
        }

    }

}
