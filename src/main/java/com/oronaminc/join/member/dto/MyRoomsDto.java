package com.oronaminc.join.member.dto;

import com.oronaminc.join.room.domain.RoomStatus;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record MyRoomsDto(
    Long roomId,
    String title,
    Long emojiCount,
    Long questions,
    LocalDate startedAt,
    RoomStatus status,
    ParticipationType participationType
) {

}
