package com.oronaminc.join.member.service;

import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.dto.MyPageType;
import com.oronaminc.join.member.dto.MyProfileGetResponse;
import com.oronaminc.join.member.dto.MyProfileUpdateRequest;
import com.oronaminc.join.member.dto.MyProfileUpdateResponse;
import com.oronaminc.join.member.dto.MyRoomsDto;
import com.oronaminc.join.member.dto.MyRoomsGetResponse;
import com.oronaminc.join.member.dto.ParticipantCountDto;
import com.oronaminc.join.member.mapper.MyPageMapper;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.question.dao.QuestionRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
            memberService.findById(memberId).getNickname(),
            createdRoomCount,
            joinedRoomCount
        );
    }

    @Transactional
    public MyProfileUpdateResponse updateMyProfile(MyProfileUpdateRequest request, Long memberId) {
        Member member = memberService.findById(memberId);
        member.updateNickname(request.nickname());
        return new MyProfileUpdateResponse(memberId);
    }

    public MyRoomsGetResponse getMyRooms(Long memberId, MyPageType type, Pageable pageable) {

        Page<Participant> participants = switch (type) {
            case ALL -> participantRepository.findByMemberId(memberId, pageable);
            case CREATED -> participantRepository.findByMemberIdAndParticipantType(memberId,
                ParticipantType.PRESENTER, pageable);
            case JOINED -> participantRepository.findByMemberIdAndParticipantTypeNot(memberId,
                ParticipantType.PRESENTER, pageable);
        };

        List<Long> roomIds = participants.stream()
            .map(p -> p.getRoom().getId())
            .toList();
        List<Object[]> questionCounts = questionRepository.countByRoomIds(roomIds);
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
