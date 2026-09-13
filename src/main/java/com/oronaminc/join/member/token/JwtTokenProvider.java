package com.oronaminc.join.member.token;

import com.oronaminc.join.member.domain.MemberType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtConfiguration jwtConfiguration;

    public TokenPair generateTokenPair(JwtMemberInfo jwtMemberInfo) {

        String accessToken = issueAccessToken(jwtMemberInfo);
        String refreshToken = issueRefreshToken(jwtMemberInfo);

        return new TokenPair(accessToken, refreshToken,
            jwtConfiguration.accessTokenExpiration(), jwtConfiguration.refreshTokenExpiration());
    }

    private String issueAccessToken(JwtMemberInfo jwtMemberInfo) {
        return issue(jwtMemberInfo, jwtConfiguration.accessTokenExpiration());
    }

    private String issueRefreshToken(JwtMemberInfo jwtMemberInfo) {
        return issue(jwtMemberInfo, jwtConfiguration.refreshTokenExpiration());
    }

    private String issue(JwtMemberInfo jwtMemberInfo, Long expTime) {
        return Jwts.builder()
            .subject(jwtMemberInfo.memberId().toString())
            .claim("nickname", jwtMemberInfo.nickname())
            .claim("role", jwtMemberInfo.role())
            .issuedAt(new Date())
            .expiration(new Date(new Date().getTime() + expTime))
            .signWith(getSecretKey(), Jwts.SIG.HS256)
            .compact();
    }

    public TokenBody parseClaims(String token) {

        Jws<Claims> claims = Jwts.parser()
            .verifyWith(getSecretKey())
            .build()
            .parseSignedClaims(token);

        Claims payload = claims.getPayload();

        Long memberId = Long.parseLong(payload.getSubject());

        return new TokenBody(
            memberId,
            payload.get("nickname").toString(),
            MemberType.valueOf(payload.get("role").toString()),
            payload.getIssuedAt(),
            payload.getExpiration()
        );
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtConfiguration.secret().getBytes());
    }
}