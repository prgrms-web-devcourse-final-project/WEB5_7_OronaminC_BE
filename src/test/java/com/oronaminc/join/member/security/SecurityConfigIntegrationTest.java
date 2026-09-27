package com.oronaminc.join.member.security;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.oronaminc.join.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * SecurityConfig의 인가(authorizeHttpRequests) 규칙을 실제 필터 체인을 통해 검증하는
 * 전체 컨텍스트 통합 테스트.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SecurityConfig 인가 규칙 통합 검증")
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Authorization 헤더 없이 /api/auth/token/refresh 호출 시 401/403이 아니라 컨트롤러까지 도달한다")
    void refreshEndpoint_isPermitAll_reachesController() throws Exception {
        // when
        MvcResult result = mockMvc.perform(post("/api/auth/token/refresh"))
            .andReturn();

        // then: 보안 필터에서 차단되었다면 401/403이 반환되지만,
        // permitAll이므로 컨트롤러까지 도달해 refresh 쿠키 부재로 인한 예외 처리 응답을 받는다.
        int status = result.getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    }

    @Test
    @DisplayName("Authorization 헤더 없이 보호된 API 엔드포인트(/api/**)를 호출하면 401 JSON으로 응답한다")
    void protectedApiEndpoint_withoutAuthorizationHeader_returns401Json() throws Exception {
        // RestAuthenticationEntryPoint가 /api/** 요청에 대해 302 리다이렉트 대신
        // ErrorCode.UNAUTHORIZED_MEMBER 기반의 401 JSON을 반환해야 한다.
        mockMvc.perform(get("/api/rooms/1"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED_MEMBER.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED_MEMBER.getMessage()));
    }

    @Test
    @DisplayName("브라우저 내비게이션(Accept: text/html)으로 /api/** 가 아닌 보호된 경로를 호출하면 기존 oauth2Login 리다이렉트가 유지된다")
    void nonApiProtectedPath_fromBrowser_stillRedirectsToOAuth2Login() throws Exception {
        // "/api/**"가 아닌 경로에 대한 실제 브라우저 내비게이션(Accept: text/html)은
        // oauth2Login()의 기존 리다이렉트 흐름을 그대로 유지해야 한다.
        // (oauth2Login이 등록하는 AuthenticationEntryPoint는 Accept 헤더 기반
        // MediaTypeRequestMatcher로 "브라우저의 페이지 요청"만 선별하며, Accept 헤더가
        // 없거나 */* 인 일반 API/AJAX 요청은 애초에 이 매처에 매칭되지 않는다.
        // 이 프로젝트의 컨트롤러는 전부 /api/** 하위에 있어 실제 보호되는 비-API 페이지가
        // 없으므로, authorizeHttpRequests의 anyRequest().authenticated()에 걸리는
        // 임의의 비-API 경로로 "브라우저가 요청했다면"이라는 상황을 재현해 검증한다.)
        mockMvc.perform(get("/some-non-api-page").accept(MediaType.TEXT_HTML))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", endsWith("/oauth2/authorization/kakao")));
    }

    @Test
    @DisplayName("Accept 헤더 없이 /api/** 가 아닌 보호된 경로를 호출해도(비 브라우저 요청) 401 JSON으로 처리된다")
    void nonApiProtectedPath_withoutAcceptHeader_fallsBackTo401Json() throws Exception {
        // Accept 헤더가 없는(=브라우저의 페이지 내비게이션으로 보기 어려운) 요청은
        // oauth2Login의 매처에 매칭되지 않아, 최종 기본 처리인 401 JSON으로 응답한다.
        mockMvc.perform(get("/some-non-api-page"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED_MEMBER.getCode()));
    }
}
