package com.oronaminc.join.member.token;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtUtils {

    public static long toSeconds(long millis) {
        return millis / 1000;
    }

    public static void addRefreshTokenCookie(HttpServletResponse response, String refreshToken,
        long expiresIn) {
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("None")
            .maxAge(JwtUtils.toSeconds(expiresIn))
            .build();

        response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

}
