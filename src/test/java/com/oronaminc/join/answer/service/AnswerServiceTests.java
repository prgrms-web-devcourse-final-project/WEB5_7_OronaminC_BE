package com.oronaminc.join.answer.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerCreateRequest;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.member.service.MemberReader;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.service.QuestionReader;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.domain.RoomStatus;

@ExtendWith(MockitoExtension.class)
public class AnswerServiceTests {

    @InjectMocks
    private AnswerService answerService;

    @Mock
    private QuestionReader questionReader;
    @Mock
    private MemberReader memberReader;
    @Mock
    private ParticipantService participantService;
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private AnswerReader answerReader;


    private Member mockMember;
    private Room mockRoom;
    private Question mockQuestion;
    private Participant mockParticipant;
    private AnswerCreateRequest request;

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

        mockQuestion = Question.builder()
                .id(1L)
                .room(mockRoom)
                .emojiCount(0L)
                .content("질문입니다.")
                .member(mockMember)
                .version(1)
                .build();

        mockParticipant = Participant.builder()
                .id(1L)
                .member(mockMember)
                .room(mockRoom)
                .participantType(ParticipantType.TEAM)
                .build();

        request = new AnswerCreateRequest("답변입니다.");
    }

    @Test
    @DisplayName("정상적인 값이 들어오면 질문이 생성된다")
    void createAnswer_success() {
        // given

        Answer answer = Answer.builder()
                .id(1L)
                .member(mockMember)
                .question(mockQuestion)
                .emojiCount(0L)
                .version(1)
                .content("답변입니다")
                .build();

        given(questionReader.getByIdAndRoomId(1L, 1L)).willReturn(mockQuestion);
        given(memberReader.getById(1L)).willReturn(mockMember);
        given(answerReader.existsByQuestionIdAndMemberId(1L, 1L)).willReturn(false);
        given(answerRepository.save(any(Answer.class))).willReturn(answer);

        // when
        Answer result = answerService.create(1L, 1L, 1L, request);
        // then
        assertThat(result).isNotNull();
        assertThat(result.getQuestion()).isEqualTo(mockQuestion);
        assertThat(result.getMember()).isEqualTo(mockMember);
        assertThat(result.getContent()).isEqualTo("답변입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 member가 들어오면 예외 발생")
    void createAnswer_member_fail() {
        // given
        given(memberReader.getById(anyLong())).willThrow(new ErrorException(ErrorCode.NOT_FOUND_MEMBER));

        // when & then
        assertThatThrownBy(() -> answerService.create(1L, 1L, 1L, request))
                .isInstanceOf(ErrorException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_MEMBER);

    }

    // @Test
    // @DisplayName("존재하지 않는 room이 들어오면 예외 발생")
    // void createAnswer_room_fail() {
    //     // given
    //     given(memberReader.getById(1L)).willReturn(mockMember);
    //
    //     // when & then
    //     assertThatThrownBy(() -> answerService.create(1L, 1L, 1L, request))
    //             .isInstanceOf(ErrorException.class)
    //             .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_ROOM);
    //
    // }

    @Test
    @DisplayName("존재하지 않는 participant가 들어오면 예외 발생")
    void createAnswer_participant_fail() {
        // given
        given(memberReader.getById(1L)).willReturn(mockMember);
        given(questionReader.getByIdAndRoomId(1L, 1L)).willReturn(mockQuestion);
        willThrow(new ErrorException(ErrorCode.NOT_FOUND_PARTICIPANT))
                .given(participantService)
                .validateParticipant(1L, 1L);

        // when & then
        assertThatThrownBy(() -> answerService.create(1L, 1L, 1L, request))
                .isInstanceOf(ErrorException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_PARTICIPANT);

    }

    @Test
    @DisplayName("존재하지_않는_질문이_들어오면_예외_발생")
    void createAnswer_question_fail() {
        // given
        given(memberReader.getById(1L)).willReturn(mockMember);
        given(questionReader.getByIdAndRoomId(1L, 1L)).willThrow(new ErrorException(ErrorCode.NOT_FOUND_QUESTION));

        // when & then
        assertThatThrownBy(() -> answerService.create(1L, 1L, 1L, request))
                .isInstanceOf(ErrorException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_QUESTION);

    }

    @Test
    @DisplayName("중복_답변_남길시_예외_발생")
    void createAnswer_duplicate_fail() {
        // given
        given(memberReader.getById(1L)).willReturn(mockMember);
        given(questionReader.getByIdAndRoomId(1L, 1L)).willReturn(mockQuestion);
        given(answerReader.existsByQuestionIdAndMemberId(1L, 1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> answerService.create(1L, 1L, 1L, request))
                .isInstanceOf(ErrorException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BADREQUEST_DUPLICATION_ANSWER);

    }
}


