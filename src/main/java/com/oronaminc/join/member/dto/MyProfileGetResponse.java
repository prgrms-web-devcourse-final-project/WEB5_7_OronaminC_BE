package com.oronaminc.join.member.dto;

public record MyProfileGetResponse(
    String nickname,
    Long createdRoomCount,
    Long joinedRoomCount
) {

}
