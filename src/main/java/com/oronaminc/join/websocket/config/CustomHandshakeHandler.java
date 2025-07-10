package com.oronaminc.join.websocket.config;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Component
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

        HttpSession session = servletRequest.getSession(false);
        // 세션 없음
        if (session == null) {
            throw new ErrorException(ErrorCode.NOT_FOUND_SESSION);
        }

        Principal principal = servletRequest.getUserPrincipal();
        // 쿠키 없이 연결
        if (principal == null) {
            throw new ErrorException(ErrorCode.ACCESS_DENIED_SESSION);
        }

        return principal;
    }

}
