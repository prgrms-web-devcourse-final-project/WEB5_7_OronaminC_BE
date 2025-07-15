package com.oronaminc.join.global.service;

import com.oronaminc.join.emoji.dto.EmojiRequest;
import com.oronaminc.join.global.util.StringUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket getEmojiBucket(Long memberId, EmojiRequest emojiRequest) {
        String apiKey = StringUtil.format("{}:{}:{}", memberId, emojiRequest.targetType(),
            emojiRequest.targetId());
        return cache.computeIfAbsent(apiKey, key ->
            Bucket.builder()
                .addLimit(
                    Bandwidth.builder()
                        .capacity(3)
                        .refillIntervally(3, Duration.ofSeconds(1))
                        .build()
                )
                .build()
        );
    }
}
