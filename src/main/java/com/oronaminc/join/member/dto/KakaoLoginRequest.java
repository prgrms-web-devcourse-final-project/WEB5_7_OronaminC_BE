package com.oronaminc.join.member.dto;

public record KakaoLoginRequest(
        String code,
        String state
) {
}
