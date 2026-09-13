package com.oronaminc.join.member.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * REST API("/api/**") 요청에 대한 인증 실패 처리 전용 AuthenticationEntryPoint.
 * oauth2Login()이 기본으로 등록하는 AuthenticationEntryPoint는 인증되지 않은 요청을
 * 카카오 로그인 페이지로 302 리다이렉트시키는데, 이는 브라우저 기반 로그인 흐름에는
 * 맞지만 JWT 기반 REST API 클라이언트 입장에서는 잘못된 동작이다.
 * 이 EntryPoint는 이 프로젝트의 ErrorException/ErrorCode 컨벤션에 맞춰 401 JSON
 * 응답(ErrorResponse)을 반환한다.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.UNAUTHORIZED_MEMBER);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
