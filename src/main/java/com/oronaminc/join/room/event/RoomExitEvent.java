package com.oronaminc.join.room.event;

public record RoomExitEvent(
        Long memberId,
        Long roomId
) {
}
