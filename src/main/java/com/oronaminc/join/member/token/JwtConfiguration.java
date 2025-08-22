package com.oronaminc.join.member.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtConfiguration(
        String secret,
        Long accessTokenExpiration,
        Long refreshTokenExpiration
) {
}
