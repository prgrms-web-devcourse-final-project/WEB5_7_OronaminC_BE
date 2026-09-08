package com.oronaminc.join.answer.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.service.AnswerReader;
import com.oronaminc.join.answer.util.PermissionValidator;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.participant.service.ParticipantReader;
import com.oronaminc.join.question.domain.Question;
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
    private ParticipantReader participantReader;

    @Mock
    private AnswerReader answerReader;

    private Member questionAuthor;
    private Room room;
    private Member member;
    private Question question;
    private Answer answer;
    private Participant participant;

    @BeforeEach
    void setUp() {

        member = Member.builder().id(1L).build();
        room = Room.builder().id(1L).build();
        questionAuthor = Member.builder().id(2L).build(); // 질문 작성자는 다른 사람으로 기본 설정
        participant = Participant.builder()
            .id(1L)
            .member(member)
            .room(room)
            .participantType(ParticipantType.GUEST)
            .build();
        question = Question.builder().id(100L).member(questionAuthor).room(room).build();
        answer = Answer.builder().id(200L).question(question).member(member).build();
    }

    @Test
    @DisplayName("TEAM or PRESENTER or 작성자가 아닌 참여자가 답변시 예외 발생")
    void validateAnswerPermission_fail_not_team_or_presenter_orWriter() {
        // given
        given(participantReader.getByRoomIdAndMemberId(1L, 1L)).willReturn(participant);

        // when & then
        assertThatThrownBy(() -> permissionValidator.validateAnswerCreatePermission(1L, 1L, question))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_ROLE_ANSWER);
    }

    @Test
    @DisplayName("발표방에 존재하지 않는 participant라면 예외 발생")
    void validateAnswerPermission_fail_not_found_participant() {
        // given
        given(participantReader.getByRoomIdAndMemberId(1L, 1L))
            .willThrow(new ErrorException(ErrorCode.NOT_FOUND_PARTICIPANT));

        // when & then
        assertThatThrownBy(() -> permissionValidator.validateAnswerCreatePermission(1L, 1L, question))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_PARTICIPANT);
    }

    @Test
    @DisplayName("삭제 권한 - 질문 작성자(GUEST 포함)는 삭제 가능")
    void deletePermission_success_byQuestionWriter() {
        // given
        question = Question.builder().id(100L).member(member).room(room).build(); // 질문자 = 본인
        answer = Answer.builder().id(200L).question(question).member(Member.builder().id(999L).build()).build(); // 답변자는 본인 아님

        Participant participant = Participant.builder()
            .member(member)
            .room(room)
            .participantType(ParticipantType.GUEST)
            .build();

        given(answerReader.getById(200L)).willReturn(answer);
        given(participantReader.getByRoomIdAndMemberId(1L, 1L)).willReturn(participant);

        // when & then
        assertThatCode(() -> permissionValidator.validateAnswerDeletePermission( 200L, 1L))
            .doesNotThrowAnyException();
    }


    @Test
    @DisplayName("삭제 권한 - 팀원(TEAM)은 삭제 가능")
    void deletePermission_success_byTeam() {
        Participant participant = Participant.builder()
            .member(member)
            .room(room)
            .participantType(ParticipantType.TEAM)
            .build();

        given(answerReader.getById(200L)).willReturn(answer);
        given(participantReader.getByRoomIdAndMemberId(1L, 1L)).willReturn(participant);

        assertThatCode(() -> permissionValidator.validateAnswerDeletePermission( 200L, 1L))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("삭제 권한 - 발표자(PRESENTER)는 삭제 가능")
    void deletePermission_success_byPresenter() {
        Participant participant = Participant.builder()
            .member(member)
            .room(room)
            .participantType(ParticipantType.PRESENTER)
            .build();

        given(answerReader.getById(200L)).willReturn(answer);
        given(participantReader.getByRoomIdAndMemberId(1L, 1L)).willReturn(participant);

        assertThatCode(() -> permissionValidator.validateAnswerDeletePermission( 200L, 1L))
            .doesNotThrowAnyException();
    }


    @Test
    @DisplayName("삭제 권한 - 권한 없는 GUEST는 삭제 불가")
    void deletePermission_fail_unauthorizedGuest() {
        Participant participant = Participant.builder()
            .member(member)
            .room(room)
            .participantType(ParticipantType.GUEST)
            .build();

        given(answerReader.getById(200L)).willReturn(answer);
        given(participantReader.getByRoomIdAndMemberId(1L, 1L)).willReturn(participant);

        assertThatThrownBy(() -> permissionValidator.validateAnswerDeletePermission(200L, 1L))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_DELETE_ANSWER);
    }
}
