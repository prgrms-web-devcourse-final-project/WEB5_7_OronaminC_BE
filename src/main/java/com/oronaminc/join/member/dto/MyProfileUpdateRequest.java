package com.oronaminc.join.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

@Schema(description = "마이페이지 프로필 수정 요청 DTO")
public record MyProfileUpdateRequest(
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Length(min = 2, max = 8)
    @Schema(description = "수정할 닉네임", example = "kakao")
    String nickname
) {

}
