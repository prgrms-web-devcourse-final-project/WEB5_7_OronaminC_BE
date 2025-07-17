package com.oronaminc.join.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 존재 여부 응답 DTO")
public record ExistsMemberResponse(
    @Schema(description = "서버에 존재하는 회원 유무", example = "true")
        boolean exists
) {
}
