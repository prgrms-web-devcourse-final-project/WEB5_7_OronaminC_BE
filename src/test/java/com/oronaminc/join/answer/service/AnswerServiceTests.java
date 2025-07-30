package com.oronaminc.join.answer.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;

import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerGetResponse;
import com.oronaminc.join.answer.dto.AnswerRequest;
import com.oronaminc.join.answer.util.PermissionValidator;
import com.oronaminc.join.emoji.domain.Emoji;
import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.emoji.service.EmojiReader;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.member.service.MemberReader;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.participant.service.ParticipantReader;
import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.service.QuestionReader;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.domain.RoomStatus;
import com.oronaminc.join.room.service.RoomReader;

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
    private RoomReader roomReader;
    @Mock
    private AnswerReader answerReader;
    @Mock
    private EmojiReader emojiReader;
    @Mock
    private PermissionValidator permissionValidator;
    @Mock
    private ParticipantReader participantReader;


    private Member mockMember;
    private Room mockRoom;
    private Question mockQuestion;
    private Participant mockParticipant;
    private AnswerRequest request;
    private Emoji mockEmoji;

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

        request = new AnswerRequest("답변입니다.", mockMember.getId());
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
            .content("답변입니다.")
            .build();

        given(memberReader.getById(1L)).willReturn(mockMember);
        given(roomReader.getById(1L)).willReturn(mockRoom);
        given(questionReader.getByIdAndRoomId(1L, 1L)).willReturn(mockQuestion);
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
    @DisplayName("답변 목록 조회 - 커서 없이 최초 페이지 조회")
    void getAnswers_firstPage_success() {
        // given
        List<Answer> answers = List.of(createAnswer(100L,LocalDateTime.now()), createAnswer(99L,LocalDateTime.now() ));
        given(memberReader.getById(1L)).willReturn(mockMember);
        given(roomReader.getById(1L)).willReturn(mockRoom);
        given(questionReader.getByIdAndRoomId(1L, 1L)).willReturn(mockQuestion);
        given(answerReader.getByQuestionId(1L)).willReturn(null);
        given(answerReader.getFirstPageByQuestionId(eq(1L), any())).willReturn(answers);
        given(emojiReader.findTargetIdsByMemberAndTargetTypeInBatch(1L, TargetType.ANSWER,
            List.of(100L, 99L)))
            .willReturn(Set.of(100L));

        // when
        Slice<AnswerGetResponse> response = answerService.getAnswers(1L, 1L, 1L, null, null, 10);

        // then
        assertThat(response.getContent().get(0).answerId()).isEqualTo(100L);
        assertThat(response.getContent().get(0).isEmojied()).isTrue();
    }

    @Test
    @DisplayName("답변 목록 조회 - 커서 기준 이후 답변 조회")
    void getAnswers_cursorPaging_success() {
        // given
        List<Answer> answers = List.of(createAnswer(80L,LocalDateTime.now()), createAnswer(79L,LocalDateTime.now()));
        given(memberReader.getById(1L)).willReturn(mockMember);
        given(roomReader.getById(1L)).willReturn(mockRoom);
        given(questionReader.getByIdAndRoomId(1L, 1L)).willReturn(mockQuestion);
        given(answerReader.getByQuestionId(1L)).willReturn(null);
        given(answerReader.getAnswerByQuestionIdWithCursor(eq(1L), any(), any(), any())).willReturn(
            answers);
        given(emojiReader.findTargetIdsByMemberAndTargetTypeInBatch(1L, TargetType.ANSWER,
            List.of(80L, 79L)))
            .willReturn(Set.of());

        // when
        Slice<AnswerGetResponse> response = answerService.getAnswers(1L, 1L, 1L, 90L,
            LocalDateTime.now(), 10);

        // then
        assertThat(response.getContent().get(0).answerId()).isEqualTo(80L);
        assertThat(response.getContent().get(0).isEmojied()).isFalse();
    }

    @Test
    @DisplayName("답변 목록 조회 - 공감이 포함된 답변들 조회")
    void getAnswers_containsEmojiedAnswers() {
        // given
        List<Answer> answers = List.of(createAnswer(1L,LocalDateTime.now()), createAnswer(2L,LocalDateTime.now()));
        given(memberReader.getById(1L)).willReturn(mockMember);
        given(roomReader.getById(1L)).willReturn(mockRoom);
        given(questionReader.getByIdAndRoomId(1L, 1L)).willReturn(mockQuestion);
        given(answerReader.getByQuestionId(1L)).willReturn(null);
        given(answerReader.getFirstPageByQuestionId(eq(1L), any())).willReturn(answers);
        given(emojiReader.findTargetIdsByMemberAndTargetTypeInBatch(1L, TargetType.ANSWER,
            List.of(1L, 2L)))
            .willReturn(Set.of(2L));

        // when
        Slice<AnswerGetResponse> response = answerService.getAnswers(1L, 1L, 1L, null, null, 10);

        // then
        assertThat(response.getContent().get(0).isEmojied()).isFalse();
        assertThat(response.getContent().get(1).isEmojied()).isTrue();
    }


    @Test
    @DisplayName("답변 목록 조회 - 결과가 비어도 예외 없이 처리")
    void getAnswers_emptyList_noError() {
        // given
        given(memberReader.getById(1L)).willReturn(mockMember);
        given(roomReader.getById(1L)).willReturn(mockRoom);
        given(questionReader.getByIdAndRoomId(1L, 1L)).willReturn(mockQuestion);
        given(answerReader.getByQuestionId(1L)).willReturn(null);
        given(answerReader.getFirstPageByQuestionId(eq(1L), any())).willReturn(List.of());

        // when
        Slice<AnswerGetResponse> response = answerService.getAnswers(1L, 1L, 1L, null, null, 10);

        // then
        assertThat(response.getContent()).asInstanceOf(LIST).isEmpty();
    }

    private Answer createAnswer(Long id, LocalDateTime createdAt) {
        Answer answer = Answer.builder()
            .id(id)
            .member(mockMember)
            .question(mockQuestion)
            .content("답변입니다")
            .emojiCount(0L)
            .build();

        ReflectionTestUtils.setField(answer, "createdAt", createdAt);
        return answer;
    }

    @Test
    @DisplayName("답변 수정 - 작성자 본인이면 수정에 성공한다")
    void updateAnswer_success() {
        // given
        Answer answer = Answer.builder()
            .id(1L)
            .member(mockMember)
            .question(mockQuestion)
            .content("기존 내용")
            .build();

        given(permissionValidator.validateAnswerUpdatePermission(1L, 1L))
            .willReturn(answer);

        AnswerRequest request = new AnswerRequest("수정된 내용", 1L);

        // when
        Answer result = answerService.update(answer.getId(), answer.getMember().getId(), request);

        // then
        assertThat(result.getContent()).isEqualTo("수정된 내용");
    }


    @Test
    @DisplayName("존재하지 않는 member가 들어오면 예외 발생")
    void createAnswer_member_fail() {
        // given
        given(memberReader.getById(anyLong())).willThrow(
            new ErrorException(ErrorCode.NOT_FOUND_MEMBER));

        // when & then
        assertThatThrownBy(() -> answerService.create(1L, 1L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_MEMBER);

    }

    @Test
    @DisplayName("존재하지 않는 room이 들어오면 예외 발생")
    void createAnswer_room_fail() {
        // given
        given(memberReader.getById(1L)).willReturn(mockMember);
        given(roomReader.getById(anyLong())).willThrow(
            new ErrorException(NOT_FOUND_ROOM));
        // when & then
        assertThatThrownBy(() -> answerService.create(1L, 1L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_ROOM);

    }


    @Test
    @DisplayName("존재하지_않는_질문이_들어오면_예외_발생")
    void createAnswer_question_fail() {
        // given
        given(memberReader.getById(1L)).willReturn(mockMember);
        given(roomReader.getById(1L)).willReturn(mockRoom);
        given(questionReader.getByIdAndRoomId(1L, 1L)).willThrow(
            new ErrorException(ErrorCode.NOT_FOUND_QUESTION));

        // when & then
        assertThatThrownBy(() -> answerService.create(1L, 1L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_QUESTION);

    }

}


