package com.oronaminc.join.member.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;

public record GuestLoginRequest(
        @NotBlank
        @Length(min = 2, max = 8)
        String nickname
) {
}
