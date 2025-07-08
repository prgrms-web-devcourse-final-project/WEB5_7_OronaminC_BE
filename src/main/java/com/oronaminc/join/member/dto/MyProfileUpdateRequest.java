package com.oronaminc.join.member.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record MyProfileUpdateRequest(
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Length(min = 2, max = 8)
    String nickname
) {

}
