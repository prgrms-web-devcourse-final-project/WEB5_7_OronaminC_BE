package com.oronaminc.join.room.util;

import java.time.LocalTime;

import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.domain.RoomStatus;
import com.oronaminc.join.room.domain.RoomType;
import com.oronaminc.join.room.dto.CreateRoomRequest;
import com.oronaminc.join.room.dto.CreateRoomResponse;

public class RoomMapper {

    public static Room toRoom(CreateRoomRequest createRoomRequest, String code) {
        return Room.builder()
                .title(createRoomRequest.title())
                .description(createRoomRequest.description())
                .secretCode(code)
                .roomStatus(RoomStatus.BEFORE_START)
                .roomType(RoomType.PRIVATE)
                .emojiCount(0L)
                .participantLimit(createRoomRequest.participantLimit())
                .endedAt(createRoomRequest.endDate().atTime(LocalTime.MAX))
                .build();
    }

    public static CreateRoomResponse toCreateRoomResponse(Room room) {
        return new CreateRoomResponse(room.getId(), room.getSecretCode());
    }
}
