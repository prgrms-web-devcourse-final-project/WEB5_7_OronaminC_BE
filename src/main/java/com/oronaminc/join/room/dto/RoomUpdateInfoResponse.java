package com.oronaminc.join.room.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;

@Builder
public record RoomUpdateInfoResponse(
        String title,
        String description,
        LocalDate endDate,
        Integer participantLimit,
        List<String> teamEmail
) {
}
