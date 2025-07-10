package com.oronaminc.join.room.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.oronaminc.join.room.domain.RoomStatus;

import lombok.Builder;

@Builder
public record RoomDetailResponse(
        String title,
        String description,
        String name,
        List<String> team,
        String roomCode,
        String documentUrl,
        Integer participantCount,
        Integer participantLimit,
        Long emojiCount,
        boolean isHost,
        boolean isTeamMember,
        RoomStatus roomStatus,
        LocalDateTime createdAt
) {
}
