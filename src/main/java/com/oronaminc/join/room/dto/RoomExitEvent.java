package com.oronaminc.join.room.dto;

public record RoomExitEvent(
        Long memberId,
        Long roomId
) {
}
