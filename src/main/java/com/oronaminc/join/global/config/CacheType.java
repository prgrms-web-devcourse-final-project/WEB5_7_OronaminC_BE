package com.oronaminc.join.global.config;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CacheType {
    ROOM_BY_ID("roomById", 300, 1000),
    ROOM_BY_SECRET_CODE("roomBySecretCode", 300, 1000),
    REFRESH_LATEST("refreshLatest", 60 * 60 * 24 * 14, 100_000), // 14일
    REFRESH_BLACKLIST("refreshBlacklist", 60 * 60 * 24 * 14, 100_000);
    public final String cacheName;
    public final int expireAfterWrite;
    public final int maximumSize;

}
