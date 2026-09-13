package com.oronaminc.join.member.token;

import com.oronaminc.join.member.domain.MemberType;

public record AuthTokenResponse(
    String accessToken,
    long accessTokenExpiresIn,
    Long memberId,
    String nickname,
    MemberType role
) {

}
