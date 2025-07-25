package com.oronaminc.join.global.config;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CacheType {
    ROOM_BY_ID("roomById", 300, 1000),
    ROOM_BY_SECRET_CODE("roomBySecretCode", 300, 1000)
    ;

    public final String cacheName;
    public final long expireAfterWrite;
    public final long maximumSize;
}
