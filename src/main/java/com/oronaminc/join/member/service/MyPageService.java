package com.oronaminc.join.member.service;

import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.dto.MyPageType;
import com.oronaminc.join.member.dto.MyProfileGetResponse;
import com.oronaminc.join.member.dto.MyProfileUpdateRequest;
import com.oronaminc.join.member.dto.MyProfileUpdateResponse;
import com.oronaminc.join.member.dto.MyRoomsDto;
import com.oronaminc.join.member.dto.MyRoomsGetResponse;
import com.oronaminc.join.member.dto.ParticipantCountDto;
import com.oronaminc.join.member.dto.ParticipationType;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.question.dao.QuestionRepository;
import com.oronaminc.join.room.domain.Room;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MyPageService {

    private final ParticipantRepository participantRepository;
    private final QuestionRepository questionRepository;

    private final MemberService memberService;

    public MyProfileGetResponse getMyProfile(Long memberId) {
        Long createdRoomCount = 0L;
        Long joinedRoomCount = 0L;

        List<ParticipantCountDto> participantCounts =
            participantRepository.countByMemberIdGroupByParticipantType(memberId);

        for (ParticipantCountDto pc : participantCounts) {
            switch (pc.participantType()) {
                case PRESENTER -> createdRoomCount = pc.count();
                case TEAM, GUEST -> joinedRoomCount += pc.count();
            }
        }

        return new MyProfileGetResponse(
            memberService.getMember(memberId).getNickname(),
            createdRoomCount,
            joinedRoomCount
        );
    }

    @Transactional
    public MyProfileUpdateResponse updateMyProfile(MyProfileUpdateRequest request, Long memberId) {
        Member member = memberService.getMember(memberId);
        member.updateNickname(request.nickname());
        return new MyProfileUpdateResponse(memberId);
    }

    public MyRoomsGetResponse getMyRooms(Long memberId, MyPageType type, Pageable pageable) {

        Page<Participant> participants;
        if (type == MyPageType.CREATED) {
            participants = participantRepository.findByMemberIdAndParticipantType(memberId,
                ParticipantType.PRESENTER, pageable);
        } else if (type == MyPageType.JOINED) {
            participants = participantRepository.findByMemberIdAndParticipantTypeNot(memberId,
                ParticipantType.PRESENTER, pageable);
        } else {
            participants = participantRepository.findByMemberId(memberId, pageable);
        }

        List<Long> roomIds = participants.stream()
            .map(p -> p.getRoom().getId())
            .toList();
        Map<Long, Long> questionCounts = questionRepository.countByRoomIds(roomIds);

        Page<MyRoomsDto> response = participants.map(p -> {
            Room room = p.getRoom();
            return MyRoomsDto.builder()
                .roomId(room.getId())
                .title(room.getTitle())
                .emojiCount(room.getEmojiCount())
                .status(room.getRoomStatus())
                .startedAt(room.getCreatedAt().toLocalDate())
                .participationType(ParticipationType.from(p.getParticipantType()))
                .questions(questionCounts.getOrDefault(room.getId(), 0L))
                .build();
        });

        return MyRoomsGetResponse.builder()
            .content(response.getContent())
            .currentPage(response.getNumber())
            .size(response.getSize())
            .totalElements(response.getTotalElements())
            .totalPages(response.getTotalPages())
            .build();
    }

}
