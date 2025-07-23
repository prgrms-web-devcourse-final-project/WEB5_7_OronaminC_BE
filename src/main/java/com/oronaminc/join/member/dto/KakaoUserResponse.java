package com.oronaminc.join.member.dto;

import lombok.Builder;

@Builder
public record KakaoUserResponse(
        String email,
        String nickname,
        String profileImageUrl
) {
}
