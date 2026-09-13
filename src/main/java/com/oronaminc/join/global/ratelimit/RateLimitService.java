package com.oronaminc.join.global.ratelimit;

import io.github.bucket4j.Bucket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket getBucket(RateLimitType rateLimitType, Object... args) {
        String apiKey = rateLimitType.createKey(args);
        return cache.computeIfAbsent(apiKey, key ->
            Bucket.builder()
                .addLimit(
                    rateLimitType.getBandwidth()
                )
                .build()
        );
    }

}
