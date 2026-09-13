package com.oronaminc.join.member.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.oronaminc.join.global.config.CacheConfig;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ExceptionAdvice;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.member.token.JwtConfiguration;
import com.oronaminc.join.member.token.JwtMemberInfo;
import com.oronaminc.join.member.token.JwtTokenProvider;
import com.oronaminc.join.member.token.RefreshTokenStore;
import com.oronaminc.join.member.token.TokenPair;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * TokenController + JwtTokenProvider + RefreshTokenStore 의 실제 협업을
 * MockMvc(HTTP 레이어)를 통해 검증하는 통합 테스트.
 */
@DisplayName("TokenController /api/auth/token/refresh 통합 검증")
class TokenControllerIntegrationTest {

    private static final String SECRET =
        "token-controller-integration-test-secret-key-must-be-32-bytes-plus";

    private JwtTokenProvider jwtTokenProvider;
    private RefreshTokenStore refreshTokenStore;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
            new JwtConfiguration(SECRET, 3_600_000L, 1_209_600_000L));
        refreshTokenStore = new RefreshTokenStore(newCacheManager());
        TokenController tokenController = new TokenController(jwtTokenProvider, refreshTokenStore);

        mockMvc = MockMvcBuilders.standaloneSetup(tokenController)
            .setControllerAdvice(new ExceptionAdvice())
            .build();
    }

    // CacheConfig.cacheManager()가 반환하는 SimpleCacheManager는 InitializingBean이라
    // 스프링 컨테이너 밖에서 직접 생성할 때는 afterPropertiesSet()을 호출해줘야
    // 내부 캐시 맵이 초기화된다(호출하지 않으면 getCache()가 null을 반환한다).
    private static CacheManager newCacheManager() {
        SimpleCacheManager cacheManager = (SimpleCacheManager) new CacheConfig().cacheManager();
        cacheManager.afterPropertiesSet();
        return cacheManager;
    }

    @Test
    @DisplayName("블랙리스트에 등록된 refresh token으로 재발급을 요청하면 거부된다")
    void refresh_withBlacklistedToken_isRejected() throws Exception {
        // given
        TokenPair tokenPair = jwtTokenProvider.generateTokenPair(
            new JwtMemberInfo(1L, "유저", MemberType.MEMBER));
        refreshTokenStore.blacklist(tokenPair.refreshToken());

        // when & then
        // TokenController는 블랙리스트인 경우 ErrorException(INVALID_REFRESH_TOKEN)을 던지고,
        // ExceptionAdvice가 이를 400(Bad Request)으로 매핑한다.
        mockMvc.perform(post("/api/auth/token/refresh")
                .cookie(new Cookie("refreshToken", tokenPair.refreshToken())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REFRESH_TOKEN.getCode()));
    }

    @Test
    @DisplayName("refresh 쿠키가 없으면 재발급 요청이 400으로 거부된다")
    void refresh_withoutCookie_isRejected() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/token/refresh"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REFRESH_TOKEN.getCode()));
    }

    @Test
    @DisplayName("최신 refresh token이 아니면(재사용 등) 재발급 요청이 400으로 거부된다")
    void refresh_withStaleToken_isRejected() throws Exception {
        // given: 같은 memberId라도 클레임(nickname)이 달라 토큰 문자열 자체가 다른,
        // 더 이상 latest가 아닌 예전 refresh token을 시뮬레이션한다.
        Long memberId = 1L;
        TokenPair staleTokenPair = jwtTokenProvider.generateTokenPair(
            new JwtMemberInfo(memberId, "구유저", MemberType.MEMBER));
        TokenPair latestTokenPair = jwtTokenProvider.generateTokenPair(
            new JwtMemberInfo(memberId, "신유저", MemberType.MEMBER));
        refreshTokenStore.saveLatest(memberId, latestTokenPair.refreshToken());

        // when & then
        mockMvc.perform(post("/api/auth/token/refresh")
                .cookie(new Cookie("refreshToken", staleTokenPair.refreshToken())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REFRESH_TOKEN.getCode()));
    }

    @Test
    @DisplayName("블랙리스트에 없고 최신 상태인 refresh token은 정상적으로 재발급된다")
    void refresh_withValidLatestToken_success() throws Exception {
        // given
        Long memberId = 1L;
        TokenPair tokenPair = jwtTokenProvider.generateTokenPair(
            new JwtMemberInfo(memberId, "유저", MemberType.MEMBER));
        refreshTokenStore.saveLatest(memberId, tokenPair.refreshToken());

        // when & then
        mockMvc.perform(post("/api/auth/token/refresh")
                .cookie(new Cookie("refreshToken", tokenPair.refreshToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.memberId").value(memberId))
            .andExpect(jsonPath("$.nickname").value("유저"));
    }
}
