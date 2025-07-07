package com.oronaminc.join.member.service;

import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.dto.MyProfileGetResponse;
import com.oronaminc.join.member.dto.MyProfileUpdateRequest;
import com.oronaminc.join.member.dto.MyProfileUpdateResponse;
import com.oronaminc.join.member.dto.ParticipantCountDto;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MyPageService {

    private final ParticipantRepository participantRepository;

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


}
