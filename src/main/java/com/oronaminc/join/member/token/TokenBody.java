package com.oronaminc.join.member.token;

import com.oronaminc.join.member.domain.MemberType;

import java.util.Date;

public record TokenBody(
        Long memberId,
        String nickname,
        MemberType role,
        Date issuedAt,
        Date expiration
) {
}
