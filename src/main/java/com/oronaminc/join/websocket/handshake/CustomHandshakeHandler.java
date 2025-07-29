package com.oronaminc.join.websocket.handshake;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.security.MemberDetails;
import com.oronaminc.join.websocket.stomp.StompPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

        HttpSession session = servletRequest.getSession(false);
        // 세션 없음
        if (session == null) {
            throw new ErrorException(ErrorCode.NOT_FOUND_SESSION);
        }

        // oauth 처리
        Authentication authentication = (Authentication) session.getAttribute(
            "SPRING_SECURITY_CONTEXT_AUTHENTICATION");

        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }

        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof MemberDetails memberDetails) {
                return new StompPrincipal(String.valueOf(memberDetails.getId()));
            }
        }

        // 비회원 처리
        String guestId = (String) session.getAttribute("LOGIN_USER_ID");

        if (guestId != null) {
            return new StompPrincipal(guestId);
        }

        // fallback 경로로 전송
        return null;
    }
}
