package com.oronaminc.join.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유효한 세션으로 로그인한 사용자 정보 응답 DTO")
public record SessionInfoResponse(
    @Schema(description = "Kakao 회원 ID", example = "1")
    Long id,
    @Schema(description = "Kakao 회원 이름", example = "카카오")
    String name,
    @Schema(description = "Kakao 회원 닉네임", example = "kakao")
    String nickname,
    @Schema(description = "Kakao 회원 역할", example = "MEMBER")
    String role
) {

}
