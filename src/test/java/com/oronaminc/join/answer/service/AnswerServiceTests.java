package com.oronaminc.join.answer.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerCreateRequest;
import com.oronaminc.join.answer.dto.AnswerGetResponse;
import com.oronaminc.join.emoji.domain.Emoji;
import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.emoji.repository.EmojiRepository;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.question.dao.QuestionRepository;
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
public class AnswerServiceTests {

    @InjectMocks
    private AnswerService answerService;

    @Mock private QuestionRepository questionRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private ParticipantRepository participantRepository;
    @Mock private AnswerRepository answerRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private EmojiRepository emojiRepository;

    private Member mockMember;
    private Room mockRoom;
    private Question mockQuestion;
    private Participant mockParticipant;
    private AnswerCreateRequest request;
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


        given(questionRepository.findByIdAndRoomId(1L, 1L)).willReturn(Optional.of(mockQuestion));
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
        given(roomRepository.findById(1L)).willReturn(Optional.of(mockRoom));
        given(participantRepository.existsByRoomIdAndMemberId(1L, 1L)).willReturn(true);
        given(answerRepository.existsByQuestionIdAndMemberId(1L, 1L)).willReturn(false);
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
            .emojiCount(0L)
            .version(0)
            .build();

        mockEmoji = Emoji.builder()
            .id(1L)
            .member(mockMember)
            .targetType(TargetType.ANSWER)
            .targetId(mockAnswer.getId())
            .build();

        // mocking
        given(memberRepository.findById(memberId)).willReturn(Optional.of(mockMember));
        given(roomRepository.findById(roomId)).willReturn(Optional.of(mockRoom));
        given(questionRepository.findByIdAndRoomId(questionId, roomId)).willReturn(Optional.of(mockQuestion));
        given(answerRepository.findByQuestionId(questionId)).willReturn(Optional.of(mockAnswer));
        given(emojiRepository.countByTargetIdAndTargetType(mockAnswer.getId(), TargetType.ANSWER)).willReturn(5);
        given(emojiRepository.findByMemberIdAndTargetIdAndTargetType(memberId, mockAnswer.getId(), TargetType.ANSWER)).willReturn(Optional.of(mockEmoji));

        // when
        AnswerGetResponse response = answerService.getAnswer(roomId, questionId, memberId);

        // then
        assertThat(response.answerId()).isEqualTo(mockAnswer.getId());
        assertThat(response.content()).isEqualTo(mockAnswer.getContent());
        assertThat(response.emojiCount()).isEqualTo(5);
        assertThat(response.Emojied()).isTrue();
        assertThat(response.writer().memberId()).isEqualTo(mockMember.getId());
        assertThat(response.writer().nickname()).isEqualTo(mockMember.getNickname());
    }

    @Test
    @DisplayName("존재하지 않는 member가 들어오면 예외 발생")
    void createAnswer_member_fail() {
        // given
        given(memberRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> answerService.create(1L, 1L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_MEMBER);

    }

    @Test
    @DisplayName("존재하지 않는 room이 들어오면 예외 발생")
    void createAnswer_room_fail() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
        given(roomRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> answerService.create(1L, 1L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_ROOM);

    }

    @Test
    @DisplayName("존재하지 않는 participant가 들어오면 예외 발생")
    void createAnswer_participant_fail() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
        given(roomRepository.findById(1L)).willReturn(Optional.of(mockRoom));
        given(questionRepository.findByIdAndRoomId(1L, 1L)).willReturn(Optional.of(mockQuestion));
        given(participantRepository.existsByRoomIdAndMemberId(1L, 1L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> answerService.create(1L, 1L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_PARTICIPANT);

    }

    @Test
    @DisplayName("존재하지_않는_질문이_들어오면_예외_발생")
    void createAnswer_question_fail() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
        given(roomRepository.findById(1L)).willReturn(Optional.of(mockRoom));
        given(questionRepository.findByIdAndRoomId(1L, 1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> answerService.create(1L, 1L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_ROOM_QUESTION);

    }

    @Test
    @DisplayName("중복_답변_남길시_예외_발생")
    void createAnswer_duplicate_fail() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
        given(roomRepository.findById(1L)).willReturn(Optional.of(mockRoom));
        given(questionRepository.findByIdAndRoomId(1L, 1L)).willReturn(Optional.of(mockQuestion));
        given(participantRepository.existsByRoomIdAndMemberId(1L, 1L)).willReturn(true);
        given(answerRepository.existsByQuestionIdAndMemberId(1L, 1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> answerService.create(1L, 1L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BADREQUEST_DUPLICATION_ANSWER);

    }
}


