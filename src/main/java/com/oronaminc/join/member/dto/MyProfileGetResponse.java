package com.oronaminc.join.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마이페이지 회원 프로필 조회 응답 DTO")
public record MyProfileGetResponse(
    @Schema(description = "회원 닉네임", example = "kakao")
    String nickname,
    @Schema(description = "생성한 방 수", example = "10")
    Long createdRoomCount,
    @Schema(description = "참여한 방 수", example = "3")
    Long joinedRoomCount
) {

}
