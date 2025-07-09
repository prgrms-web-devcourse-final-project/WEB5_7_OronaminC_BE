package com.oronaminc.join.member.dto;

import jakarta.validation.constraints.Email;

public record ExistsMemberRequest(
        @Email
        String email
) {
}
