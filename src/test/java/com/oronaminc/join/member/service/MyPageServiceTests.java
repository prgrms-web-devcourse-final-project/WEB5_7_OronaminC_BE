package com.oronaminc.join.member.service;

import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.dto.MyProfileGetResponse;
import com.oronaminc.join.member.dto.ParticipantCountDto;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.participant.domain.ParticipantType;
import java.util.List;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTests {

    @Mock
    private MemberService memberService;

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private MyPageService myPageService;

    @Test
    @DisplayName("해당 멤버의 참가방이 생성한방 1개, 팀원 1개, 게스트 1개일 경우 생성한방 1개, 참여한방 2개가 닉네임과 반환될 것이다.")
    void get() {

        // given
        Member member = Member.builder().build();

        List<ParticipantCountDto> pc = List.of(
            new ParticipantCountDto(ParticipantType.PRESENTER, 1L),
            new ParticipantCountDto(ParticipantType.TEAM, 1L),
            new ParticipantCountDto(ParticipantType.GUEST, 1L)
        );

        when(memberService.getMember(member.getId())).thenReturn(member);
        when(participantRepository.countByMemberIdGroupByParticipantType(member.getId()))
            .thenReturn(pc);

        // when
        MyProfileGetResponse myProfile = myPageService.getMyProfile(member.getId());

        // then
        assertThat(myProfile.nickname()).isEqualTo(member.getNickname());
        assertThat(myProfile.createdRoomCount()).isEqualTo(1L);
        assertThat(myProfile.joinedRoomCount()).isEqualTo(2L);

    }
}