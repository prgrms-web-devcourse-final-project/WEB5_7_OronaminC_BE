package com.oronaminc.join.member.token;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {
    private final CacheManager cacheManager;

    private Cache latest() {
        return cacheManager.getCache("refreshLatest");
    }
    private Cache blacklist() {
        return cacheManager.getCache("refreshBlacklist");
    }

    public void saveLatest(Long memberId, String refreshToken) {
        latest().put(key(memberId), refreshToken);
    }

    public boolean isLatest(Long memberId,  String refreshToken) {
        String stored = latest().get(key(memberId), String.class);
        return Objects.equals(stored, refreshToken);
    }

    public boolean isBlacklisted(String refreshToken) {
        Boolean v = blacklist().get(refreshToken, Boolean.class);
        return v != null && v;
    }

    private String key(Long memberId) {
        return "refresh:" + memberId;
    }
}
