package com.oronaminc.join.answer.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.oronaminc.join.answer.util.PermissionValidator;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.domain.RoomStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PermissionValidTests {

    @InjectMocks
    private PermissionValidator permissionValidator;

    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock private RoomRepository roomRepository;

    private Member mockMember;
    private Room mockRoom;

    @BeforeEach
    void setUp() {
        mockMember = Member.builder()
            .id(1L)
            .email("user@email.com")
            .nickname("유저")
            .memberType(MemberType.MEMBER)
            .build();

        mockRoom = Room.builder()
            .id(1L)
            .title("제목")
            .description("내용")
            .secretCode("123456")
            .emojiCount(0L)
            .participantLimit(0)
            .endedAt(LocalDateTime.now())
            .version(1)
            .roomStatus(RoomStatus.STARTED)
            .build();

    }

    @Test
    @DisplayName("TEAM or PRESENTER가 아닌 GUEST가 답변시 예외 발생")
    void validateAnswerPermission_fail_not_team_or_presenter() {
        //given
        Participant participant = Participant.builder()
            .id(1L)
            .member(mockMember)
            .room(mockRoom)
            .participantType(ParticipantType.GUEST)
            .build();

        given(participantRepository.findByRoomIdAndMemberId(1L, 1L)).willReturn(Optional.of(participant));

        // when & then
        assertThatThrownBy(() -> permissionValidator.validateAnswerPermission(1L, 1L))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_ROLE_ANSWER);

    }


    @Test
    @DisplayName("발표방에 존재하지 않는 participant라면 예외 발생")
    void validateAnswerPermission_fail_not_found_participant() {
        //given
        given(participantRepository.findByRoomIdAndMemberId(1L, 1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> permissionValidator.validateAnswerPermission(1L, 1L))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_PARTICIPANT);

    }
}
