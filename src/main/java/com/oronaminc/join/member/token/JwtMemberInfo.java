package com.oronaminc.join.member.token;

import com.oronaminc.join.member.domain.MemberType;

public record JwtMemberInfo(
        Long memberId,
        String nickname,
        MemberType role
) {
}
