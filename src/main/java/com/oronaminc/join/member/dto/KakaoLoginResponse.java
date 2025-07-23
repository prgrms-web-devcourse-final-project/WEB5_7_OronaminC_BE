package com.oronaminc.join.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record KakaoLoginResponse(
        @Schema(description = "회원 id", example = "1001")
        Long id
) {
}
