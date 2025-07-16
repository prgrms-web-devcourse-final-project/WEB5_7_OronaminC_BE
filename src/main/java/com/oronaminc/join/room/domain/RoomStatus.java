package com.oronaminc.join.room.domain;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RoomStatus {
    BEFORE_START(false),
    STARTED(true),
    ENDED(false)
    ;

    public final Boolean canSubscribeRoom;
}
