package com.oronaminc.join.room.dto;

import com.oronaminc.join.room.domain.RoomStatus;

public record RoomUpdateStatusRequest(
        RoomStatus roomStatus
) {
}
