package com.oronaminc.join.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "비회원 로그인 요청 DTO")
public record GuestLoginRequest(
        @NotBlank
        @Length(min = 2, max = 8)
        @Schema(description = "비회원 사용자 닉네임", example = "비회원유저")
        String nickname
) {
}
