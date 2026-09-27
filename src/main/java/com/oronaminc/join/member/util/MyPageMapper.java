package com.oronaminc.join.member.util;

import com.oronaminc.join.member.dto.MyRoomsDto;
import com.oronaminc.join.member.dto.MyRoomsGetResponse;
import com.oronaminc.join.member.dto.ParticipationType;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.room.domain.Room;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.Map;

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
        LocalDate date;
        if (p.getParticipantType() == ParticipantType.PRESENTER) {
            date = room.getCreatedAt().toLocalDate();
        } else {
            date = p.getCreatedAt().toLocalDate();
        }
        return MyRoomsDto.builder()
                .roomId(room.getId())
                .title(room.getTitle())
                .emojiCount(room.getEmojiCount())
                .status(room.getRoomStatus())
                .startedAt(date)
                .participationType(ParticipationType.from(p.getParticipantType()))
                .questions(countMap.getOrDefault(room.getId(), 0L))
                .build();
    }
}


