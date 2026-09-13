package com.oronaminc.join.member.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.member.token.JwtConfiguration;
import com.oronaminc.join.member.token.JwtMemberInfo;
import com.oronaminc.join.member.token.JwtTokenProvider;
import com.oronaminc.join.member.token.TokenPair;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("JwtAuthenticationFilter 인증 처리 검증")
class JwtAuthenticationFilterTest {

    private static final String SECRET =
        "jwt-authentication-filter-test-secret-key-must-be-at-least-32-bytes-long";

    private JwtTokenProvider jwtTokenProvider;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
            new JwtConfiguration(SECRET, 3_600_000L, 1_209_600_000L));
        filter = new JwtAuthenticationFilter(jwtTokenProvider);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 JWT로 요청하면 SecurityContext에 MemberDetails principal이 JWT 값과 동일하게 설정된다")
    void validJwt_setsMemberDetailsPrincipal() throws Exception {
        // given
        TokenPair tokenPair = jwtTokenProvider.generateTokenPair(
            new JwtMemberInfo(42L, "닉네임", MemberType.MEMBER));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokenPair.accessToken());
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(MemberDetails.class);

        MemberDetails principal = (MemberDetails) authentication.getPrincipal();
        assertThat(principal.getId()).isEqualTo(42L);
        assertThat(principal.getNickname()).isEqualTo("닉네임");
        assertThat(principal.getRole()).isEqualTo(MemberType.MEMBER.name());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("만료된 JWT로 요청하면 인증이 설정되지 않는다")
    void expiredJwt_doesNotAuthenticate() throws Exception {
        // given: 발급과 동시에 만료되도록 만료 시간을 음수로 설정
        JwtTokenProvider expiredIssuer = new JwtTokenProvider(
            new JwtConfiguration(SECRET, -1_000L, -1_000L));
        String expiredToken = expiredIssuer.generateTokenPair(
            new JwtMemberInfo(1L, "만료유저", MemberType.MEMBER)).accessToken();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + expiredToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("서명이 일치하지 않는 JWT로 요청하면 인증이 설정되지 않는다")
    void invalidSignatureJwt_doesNotAuthenticate() throws Exception {
        // given: 서로 다른 secret으로 발급된 토큰
        JwtTokenProvider otherIssuer = new JwtTokenProvider(
            new JwtConfiguration(
                "completely-different-secret-key-for-signature-mismatch-test-case",
                3_600_000L, 1_209_600_000L));
        String tokenWithWrongSignature = otherIssuer.generateTokenPair(
            new JwtMemberInfo(1L, "유저", MemberType.MEMBER)).accessToken();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokenWithWrongSignature);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
