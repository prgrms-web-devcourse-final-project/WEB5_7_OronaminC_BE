package com.oronaminc.join.member.security;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.oronaminc.join.global.config.CacheConfig;
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
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@DisplayName("AuthController 로그아웃 통합 검증")
class AuthControllerIntegrationTest {

    private static final String SECRET =
        "auth-controller-integration-test-secret-key-must-be-32-bytes-plus";

    private JwtTokenProvider jwtTokenProvider;
    private RefreshTokenStore refreshTokenStore;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
            new JwtConfiguration(SECRET, 3_600_000L, 1_209_600_000L));
        refreshTokenStore = new RefreshTokenStore(newCacheManager());
        AuthService authService = mock(AuthService.class); // logout()에서는 사용되지 않는다

        AuthController authController = new AuthController(authService, jwtTokenProvider, refreshTokenStore);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
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
    @DisplayName("로그아웃 응답의 Set-Cookie 헤더에 refreshToken=; Max-Age=0 이 포함된다")
    void logout_setsExpiredRefreshTokenCookie() throws Exception {
        // given
        TokenPair tokenPair = jwtTokenProvider.generateTokenPair(
            new JwtMemberInfo(1L, "유저", MemberType.MEMBER));
        refreshTokenStore.saveLatest(1L, tokenPair.refreshToken());

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                .cookie(new Cookie("refreshToken", tokenPair.refreshToken())))
            .andExpect(status().isNoContent())
            .andExpect(header().string(HttpHeaders.SET_COOKIE,
                allOf(containsString("refreshToken="), containsString("Max-Age=0"))));

        // 로그아웃 처리된 refresh token은 블랙리스트에도 등록되어야 한다
        assertThat(refreshTokenStore.isBlacklisted(tokenPair.refreshToken())).isTrue();
    }

    @Test
    @DisplayName("refresh 쿠키가 없어도 로그아웃은 204와 만료 쿠키를 응답한다")
    void logout_withoutRefreshCookie_stillExpiresCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isNoContent())
            .andExpect(header().string(HttpHeaders.SET_COOKIE,
                allOf(containsString("refreshToken="), containsString("Max-Age=0"))));
    }
}
