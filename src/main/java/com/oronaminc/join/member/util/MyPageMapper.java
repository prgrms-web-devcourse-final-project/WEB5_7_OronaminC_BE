package com.oronaminc.join.member.util;

import com.oronaminc.join.member.dto.MyRoomsDto;
import com.oronaminc.join.member.dto.MyRoomsGetResponse;
import com.oronaminc.join.member.dto.ParticipationType;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.room.domain.Room;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MyPageMapper {
    public static MyRoomsGetResponse toMyRoomsGetResponse(Page<MyRoomsDto> response) {
        return MyRoomsGetResponse.builder()
            .content(response.getContent())
            .currentPage(response.getNumber())
            .size(response.getSize())
            .totalElements(response.getTotalElements())
            .totalPages(response.getTotalPages())
            .build();
    }

    public static MyRoomsDto toMyRoomsDto(Participant p, Map<Long, Long> countMap) {
        Room room = p.getRoom();
        return MyRoomsDto.builder()
            .roomId(room.getId())
            .title(room.getTitle())
            .emojiCount(room.getEmojiCount())
            .status(room.getRoomStatus())
            .startedAt(room.getCreatedAt().toLocalDate())
            .participationType(ParticipationType.from(p.getParticipantType()))
            .questions(countMap.getOrDefault(room.getId(), 0L))
            .build();
    }
}


