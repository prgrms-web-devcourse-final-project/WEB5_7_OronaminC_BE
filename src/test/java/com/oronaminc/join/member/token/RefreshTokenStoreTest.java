package com.oronaminc.join.member.token;

import static org.assertj.core.api.Assertions.*;

import com.oronaminc.join.global.config.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleCacheManager;

@DisplayName("RefreshTokenStore 블랙리스트 동작 검증")
class RefreshTokenStoreTest {

    private RefreshTokenStore refreshTokenStore;

    @BeforeEach
    void setUp() {
        refreshTokenStore = new RefreshTokenStore(newCacheManager());
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
    @DisplayName("blacklist 등록 전에는 isBlacklisted가 false를 반환한다")
    void isBlacklisted_returnsFalse_beforeRegistering() {
        // given
        String refreshToken = "refresh-token-not-blacklisted";

        // when & then
        assertThat(refreshTokenStore.isBlacklisted(refreshToken)).isFalse();
    }

    @Test
    @DisplayName("blacklist(refreshToken) 호출 후에는 isBlacklisted가 true를 반환한다")
    void blacklist_thenIsBlacklisted_returnsTrue() {
        // given
        String refreshToken = "refresh-token-to-blacklist";

        // when
        refreshTokenStore.blacklist(refreshToken);

        // then
        assertThat(refreshTokenStore.isBlacklisted(refreshToken)).isTrue();
    }
}
