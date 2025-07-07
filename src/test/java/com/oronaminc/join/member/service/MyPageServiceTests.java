package com.oronaminc.join.member.service;

import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.dto.MyProfileGetResponse;
import com.oronaminc.join.member.dto.MyProfileUpdateRequest;
import com.oronaminc.join.member.dto.MyProfileUpdateResponse;
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
    @DisplayName("해당 멤버의 참가방이 PRESENTER:1, TEAM:1, GUEST:1 일 때 생성:1, 참여:2 를 반환한다.")
    void getMyProfile_success_test() {

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

    @Test
    @DisplayName("해당 멤버의 닉네임이 수정된다.")
    void updateMyProfile_success_test() {

        // given
        Member member = Member.builder().nickname("min").build();

        String newNickname = "newNickname";
        MyProfileUpdateRequest request = new MyProfileUpdateRequest(newNickname);

        when(memberService.getMember(member.getId())).thenReturn(member);

        // when
        MyProfileUpdateResponse response =
            myPageService.updateMyProfile(request, member.getId());

        // then
        assertThat(response.memberId()).isEqualTo(member.getId());
        assertThat(member.getNickname()).isEqualTo(newNickname);

    }

}