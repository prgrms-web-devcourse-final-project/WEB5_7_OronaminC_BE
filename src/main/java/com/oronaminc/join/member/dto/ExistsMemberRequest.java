package com.oronaminc.join.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

@Schema(description = "회원 존재 여부 요청 DTO")
public record ExistsMemberRequest(
        @Email
        @Schema(description = "확인할 이메일", example = "naminC@kakao.com")
        String email
) {
}
