package com.oronaminc.join.member.security;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.token.AuthTokenResponse;
import com.oronaminc.join.member.token.JwtMemberInfo;
import com.oronaminc.join.member.token.JwtTokenProvider;
import com.oronaminc.join.member.token.JwtUtils;
import com.oronaminc.join.member.token.RefreshTokenStore;
import com.oronaminc.join.member.token.TokenBody;
import com.oronaminc.join.member.token.TokenPair;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/token")
@RequiredArgsConstructor
public class TokenController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    @PostMapping("/refresh")
    public AuthTokenResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refresh = extraRefreshCookie(request);
        if (refresh == null) throw new ErrorException(ErrorCode.INVALID_REFRESH_TOKEN);

        if (refreshTokenStore.isBlacklisted(refresh)) throw new ErrorException(ErrorCode.INVALID_REFRESH_TOKEN);

        TokenBody body = jwtTokenProvider.parseClaims(refresh);
        if (!refreshTokenStore.isLatest(body.memberId(), refresh)) throw new ErrorException(ErrorCode.INVALID_REFRESH_TOKEN);

        TokenPair tokenPair = jwtTokenProvider.generateTokenPair(new JwtMemberInfo(body.memberId(),
            body.nickname(), body.role()));
        refreshTokenStore.blacklist(refresh);
        refreshTokenStore.saveLatest(body.memberId(), tokenPair.refreshToken());
        JwtUtils.addRefreshTokenCookie(response, tokenPair.refreshToken(), tokenPair.refreshTokenExpiresIn());

        return new AuthTokenResponse(tokenPair.accessToken(), tokenPair.accessTokenExpiresIn(), body.memberId(), body.nickname(), body.role());
    }

    private String extraRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}
