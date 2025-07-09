package com.oronaminc.join.room.dto;

public record CreateRoomResponse(
        Long roomId,
        String secretCode
) {
}
