package com.oronaminc.join.answer.service;

import static com.oronaminc.join.global.exception.ErrorCode.NOT_FOUND_ROOM;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerRequest;
import com.oronaminc.join.answer.dto.AnswerGetResponse;
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
import com.oronaminc.join.question.service.QuestionService;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.domain.RoomStatus;
import com.oronaminc.join.room.service.RoomReader;
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

        request = new AnswerRequest("답변입니다.");
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

        given(memberReader.getById(1L)).willReturn(mockMember);
        given(roomReader.getById(1L)).willReturn(mockRoom);
        given(questionReader.getByIdAndRoomId(1L, 1L)).willReturn(mockQuestion);
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
    @DisplayName("답변 조회 성공")
    void getAnswer_success() {
        // given
        Long memberId = 1L;
        Long roomId = 1L;
        Long questionId = 1L;

        Answer mockAnswer = Answer.builder()
            .id(10L)
            .question(mockQuestion)
            .member(mockMember)
            .content("답변입니다.")
            .emojiCount(5L)
            .version(0)
            .build();

        mockEmoji = Emoji.builder()
            .id(1L)
            .member(mockMember)
            .targetType(TargetType.ANSWER)
            .targetId(mockAnswer.getId())
            .build();

        // mocking
        given(memberReader.getById(memberId)).willReturn(mockMember);
        given(roomReader.getById(roomId)).willReturn(mockRoom);
        given(questionReader.getByIdAndRoomId(questionId, roomId)).willReturn(mockQuestion);
        given(answerReader.getByQuestionId(questionId)).willReturn(mockAnswer);
        given(emojiReader.findByMemberIdAndTargetIdAndTargetType(memberId, mockAnswer.getId(),
            TargetType.ANSWER)).willReturn(Optional.of(mockEmoji));

        // when
        AnswerGetResponse response = answerService.getAnswer(roomId, questionId, memberId);

        // then
        assertThat(response.answerId()).isEqualTo(mockAnswer.getId());
        assertThat(response.content()).isEqualTo(mockAnswer.getContent());
        assertThat(response.emojiCount()).isEqualTo(5L);
        assertThat(response.Emojied()).isTrue();
        assertThat(response.writer().memberId()).isEqualTo(mockMember.getId());
        assertThat(response.writer().nickname()).isEqualTo(mockMember.getNickname());
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

        given(answerReader.getById(1L)).willReturn(answer);
        AnswerRequest request = new AnswerRequest("수정된 내용");

        // when
        Answer result = answerService.update(answer.getId(), request);

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
    @DisplayName("존재하지 않는 participant가 들어오면 예외 발생")
    void createAnswer_participant_fail() {
        // given
        given(memberReader.getById(1L)).willReturn(mockMember);
        given(roomReader.getById(1L)).willReturn(mockRoom);
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
        given(questionReader.getByIdAndRoomId(1L, 1L)).willThrow(
            new ErrorException(ErrorCode.NOT_FOUND_QUESTION));

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
        given(roomReader.getById(1L)).willReturn(mockRoom);
        given(questionReader.getByIdAndRoomId(1L, 1L)).willReturn(mockQuestion);
        given(answerReader.existsByQuestionIdAndMemberId(1L, 1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> answerService.create(1L, 1L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BADREQUEST_DUPLICATION_ANSWER);

    }
}


