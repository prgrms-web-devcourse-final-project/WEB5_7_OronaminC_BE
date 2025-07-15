package com.oronaminc.join.room.util;

import java.time.LocalTime;
import java.util.List;

import com.oronaminc.join.document.domain.Document;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.domain.RoomStatus;
import com.oronaminc.join.room.domain.RoomType;
import com.oronaminc.join.room.dto.*;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static RoomDetailResponse toRoomDetailResponse(Room room, Participant presenter, List<Participant> team, String presignedUrl, Long memberId) {
        return RoomDetailResponse.builder()
                .title(room.getTitle())
                .description(room.getDescription())
                .name(presenter.getMember().getNickname())
                .team(team.stream().map(participant -> participant.getMember().getNickname()).toList())
                .roomCode(room.getSecretCode())
                .presignedUrl(presignedUrl)
                .participantCount(0)
                .participantLimit(room.getParticipantLimit())
                .emojiCount(room.getEmojiCount())
                .isHost(presenter.getMember().getId().equals(memberId))
                .isTeamMember(team.stream()
                        .map(Participant::getMember)
                        .anyMatch(member -> member.getId().equals(memberId))
                )
                .roomStatus(room.getRoomStatus())
                .createdAt(room.getCreatedAt())
                .build();
    }

    public static RoomUpdateInfoResponse toRoomUpdateInfoResponse(Room room, List<Participant> team) {
        return RoomUpdateInfoResponse.builder()
                .title(room.getTitle())
                .description(room.getDescription())
                .endDate(room.getEndedAt().toLocalDate())
                .participantLimit(room.getParticipantLimit())
                .teamEmail(team.stream().map(teamParticipant -> teamParticipant.getMember().getEmail()).toList())
                .build();
    }

    public static ReportResponse toReportResponse(Room room, Long totalView,Long totalQuestions, Double answerRate, List<TopQnADto> top3QnA) {
        return ReportResponse.builder()
                .roomId(room.getId())
                .title(room.getTitle())
                .totalView(totalView)
                .totalQuestions(totalQuestions)
                .answerRate(answerRate)
                .totalEmojis(room.getEmojiCount())
                .topQnA(top3QnA)
                .build();
    }
}
