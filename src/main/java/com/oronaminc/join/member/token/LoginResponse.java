package com.oronaminc.join.member.token;

public record LoginResponse(
    AuthTokenResponse authTokenResponse,
    String refreshToken,
    long refreshTokenExpiresIn
) {

}
