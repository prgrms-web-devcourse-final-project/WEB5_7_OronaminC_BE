package com.oronaminc.join.member.dto;

public record SessionInfoResponse(
        Long id,
        String name,
        String nickname,
        String role
) {
}
