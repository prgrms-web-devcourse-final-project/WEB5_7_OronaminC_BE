package com.oronaminc.join.member.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.dto.MyPageType;
import com.oronaminc.join.member.dto.MyProfileGetResponse;
import com.oronaminc.join.member.dto.MyProfileUpdateRequest;
import com.oronaminc.join.member.dto.MyRoomsDto;
import com.oronaminc.join.member.dto.MyRoomsGetResponse;
import com.oronaminc.join.member.dto.ParticipantCountDto;
import com.oronaminc.join.member.mapper.MyPageMapper;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.participant.service.ParticipantReader;
import com.oronaminc.join.question.service.QuestionReader;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MyPageService {

    private final QuestionReader questionReader;
    private final MemberReader memberReader;
    private final ParticipantReader participantReader;

    public MyProfileGetResponse getMyProfile(Long memberId) {
        Long createdRoomCount = 0L;
        Long joinedRoomCount = 0L;

        List<ParticipantCountDto> participantCounts =
                participantReader.countByMemberIdGroupByParticipantType(memberId);

        for (ParticipantCountDto pc : participantCounts) {
            switch (pc.participantType()) {
                case PRESENTER -> createdRoomCount = pc.count();
                case TEAM, GUEST -> joinedRoomCount += pc.count();
            }
        }

        return new MyProfileGetResponse(
            memberReader.getById(memberId).getNickname(),
            createdRoomCount,
            joinedRoomCount
        );
    }

    @Transactional
    public void updateMyProfile(MyProfileUpdateRequest request, Long memberId) {
        Member member = memberReader.getById(memberId);
        member.updateNickname(request.nickname());
    }

    public MyRoomsGetResponse getMyRooms(Long memberId, MyPageType type, Pageable pageable) {

        Page<Participant> participants = switch (type) {
            case ALL -> participantReader.findByMemberId(memberId, pageable);
            case CREATED -> participantReader.findByMemberIdAndParticipantType(memberId,
                ParticipantType.PRESENTER, pageable);
            case JOINED -> participantReader.findByMemberIdAndParticipantTypeNot(memberId,
                ParticipantType.PRESENTER, pageable);
        };

        List<Long> roomIds = participants.stream()
            .map(p -> p.getRoom().getId())
            .toList();
        List<Object[]> questionCounts = questionReader.countByRoomIds(roomIds);
        Map<Long, Long> countMap = questionCounts.stream()
            .collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (Long) row[1]
            ));

        Page<MyRoomsDto> response = participants.map(p ->
            MyPageMapper.toMyRoomsDto(p, countMap)
        );

        return MyPageMapper.toMyRoomsGetResponse(response);
    }

}
