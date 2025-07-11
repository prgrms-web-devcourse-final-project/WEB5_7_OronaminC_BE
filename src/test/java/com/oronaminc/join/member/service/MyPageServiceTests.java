package com.oronaminc.join.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.dto.MyPageType;
import com.oronaminc.join.member.dto.MyProfileGetResponse;
import com.oronaminc.join.member.dto.MyProfileUpdateRequest;
import com.oronaminc.join.member.dto.MyRoomsGetResponse;
import com.oronaminc.join.member.dto.ParticipantCountDto;
import com.oronaminc.join.member.dto.ParticipationType;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.question.dao.QuestionRepository;
import com.oronaminc.join.room.domain.Room;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTests {

    @Mock
    private MemberService memberService;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private MemberReader memberReader;

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

        when(memberReader.getById(member.getId())).thenReturn(member);
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
    @DisplayName("해당 멤버의 참가방이 존재하지 않으면 생성:0, 참여:0 을 반환한다.")
    void getMyProfile_success_test2() {

        // given
        Member member = Member.builder().build();

        List<ParticipantCountDto> pc = List.of();

        when(memberReader.getById(member.getId())).thenReturn(member);
        when(participantRepository.countByMemberIdGroupByParticipantType(member.getId()))
            .thenReturn(pc);

        // when
        MyProfileGetResponse myProfile = myPageService.getMyProfile(member.getId());

        // then
        assertThat(myProfile.nickname()).isEqualTo(member.getNickname());
        assertThat(myProfile.createdRoomCount()).isEqualTo(0L);
        assertThat(myProfile.joinedRoomCount()).isEqualTo(0L);

    }

    @Test
    @DisplayName("해당 멤버의 닉네임이 수정된다.")
    void updateMyProfile_success_test() {

        // given
        Member member = Member.builder().nickname("min").build();

        String newNickname = "newNickname";
        MyProfileUpdateRequest request = new MyProfileUpdateRequest(newNickname);

        when(memberReader.getById(member.getId())).thenReturn(member);

        // when
        myPageService.updateMyProfile(request, member.getId());

        // then
        assertThat(member.getNickname()).isEqualTo(newNickname);

    }

    @Test
    @DisplayName("해당 멤버의 모든 참여방 조회")
    void getMyRooms_success_test() {

        // given
        MyPageType type = MyPageType.ALL;
        Member member = Member.builder().build();
        Long memberId = member.getId();
        Pageable pageable = PageRequest.of(0, 10);

        Room room1 = Room.builder()
            .title("~1~의 정석")
            .build();
        Room room2 = Room.builder().title("~2~의 정석").build();
        Room room3 = Room.builder().title("~3~의 정석").build();
        ReflectionTestUtils.setField(room1, "id", 100L);
        ReflectionTestUtils.setField(room2, "id", 200L);
        ReflectionTestUtils.setField(room3, "id", 300L);
        ReflectionTestUtils.setField(room1, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(room2, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(room3, "createdAt", LocalDateTime.now());

        Participant participant1 = Participant.builder()
            .room(room1)
            .member(member)
            .participantType(ParticipantType.PRESENTER)
            .build();
        Participant participant2 = Participant.builder()
            .room(room2)
            .member(member)
            .participantType(ParticipantType.TEAM)
            .build();
        Participant participant3 = Participant.builder()
            .room(room3)
            .member(member)
            .participantType(ParticipantType.GUEST)
            .build();

        List<Participant> pc = List.of(participant1, participant2, participant3);
        Page<Participant> participantPage = new PageImpl<>(pc, pageable, 1);

        List<Long> roomIds = List.of(room1.getId(), room2.getId(), room3.getId());
        List<Object[]> questions = List.of(
            new Object[]{room1.getId(), 1L},
            new Object[]{room2.getId(), 2L},
            new Object[]{room3.getId(), 3L}
        );

        when(participantRepository.findByMemberId(memberId, pageable))
            .thenReturn(participantPage);
        when(questionRepository.countByRoomIds(roomIds)).thenReturn(questions);

        // when
        MyRoomsGetResponse result = myPageService.getMyRooms(memberId, type, pageable);

        // then
        assertThat(result.content()).hasSize(3);
        assertThat(result.content().getFirst().roomId()).isEqualTo(100L);
        assertThat(result.content().getFirst().title()).isEqualTo("~1~의 정석");
        assertThat(result.content().getFirst().participationType()).isEqualTo(
            ParticipationType.CREATED);
        assertThat(result.content().get(1).participationType()).isEqualTo(ParticipationType.JOINED);

    }


}