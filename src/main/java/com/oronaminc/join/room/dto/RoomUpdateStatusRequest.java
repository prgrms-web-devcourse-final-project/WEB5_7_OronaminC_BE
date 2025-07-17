package com.oronaminc.join.room.dto;

import com.oronaminc.join.room.domain.RoomStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record RoomUpdateStatusRequest(
        @Schema(description = "BEFORE_START, STARTED, ENDED", example = "BEFORE_START")
        RoomStatus roomStatus
) {
}
