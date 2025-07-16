package com.oronaminc.join.global.ratelimit;

import com.oronaminc.join.global.util.StringUtil;
import io.github.bucket4j.Bandwidth;
import java.time.Duration;
import lombok.Getter;

public enum RateLimitType {
    QUESTION(
        "QUESTION:{}:{}",
        Bandwidth.builder()
            .capacity(5)
            .refillGreedy(5, Duration.ofSeconds(15))
            .build()
    ),
    EMOJI(
        "EMOJI:{}:{}:{}",
        Bandwidth.builder()
            .capacity(3)
            .refillIntervally(3, Duration.ofSeconds(1))
            .build()
    );

    private final String format;

    @Getter
    private final Bandwidth bandwidth;

    RateLimitType(String format, Bandwidth bandwidth) {
        this.format = format;
        this.bandwidth = bandwidth;
    }

    public String createKey(Object... args) {
        return StringUtil.format(format, args);
    }

}
