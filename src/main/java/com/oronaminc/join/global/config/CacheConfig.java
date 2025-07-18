package com.oronaminc.join.global.config;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        List<CaffeineCache> caches = Arrays.stream(CacheType.values())
                .map(cache -> new CaffeineCache(
                        cache.cacheName,
                        Caffeine.newBuilder()
                                .expireAfterWrite(cache.expireAfterWrite, TimeUnit.SECONDS)
                                .maximumSize(cache.maximumSize)
                                .build()
                    )
                )
                .toList();

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caches);

        return cacheManager;
    }
}
