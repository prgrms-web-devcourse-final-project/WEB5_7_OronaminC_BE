package com.oronaminc.join.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비회원 로그인 응답 DTO")
public record GuestLoginResponse(
        @Schema(description = "비회원 id", example = "1001")
        Long id
) {
}
