package com.oronaminc.join.member.token;

public record TokenPair(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {
}
