package com.oronaminc.join.question.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import com.oronaminc.join.participant.service.ParticipantReader;
import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.member.service.MemberReader;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.question.dao.QuestionRepository;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.domain.QuestionSort;
import com.oronaminc.join.question.dto.QuestionAssembleResponse;
import com.oronaminc.join.question.dto.QuestionCreateRequest;
import com.oronaminc.join.question.dto.QuestionFlatResponse;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.domain.RoomStatus;
import com.oronaminc.join.room.service.RoomReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class QuestionServiceTests {

    @InjectMocks
    private QuestionService questionService;

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private RoomReader roomReader;
    @Mock
    private MemberReader memberReader;
    @Mock
    private ParticipantReader participantReader;
    @Mock
    private ParticipantService participantService;
    @Mock
    private QuestionReader questionReader;

    private Room mockRoom;
    private Member mockMember;
    private QuestionCreateRequest request;
    private QuestionFlatResponse mockQ1;
    private QuestionFlatResponse mockQ2;

    @BeforeEach
    void setUp() {

        mockMember = Member.builder()
            .id(1L)
            .email("user@email.com")
            .nickname("테스트유저")
            .memberType(MemberType.MEMBER)
            .profileImage("")
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

        request = new QuestionCreateRequest("질문입니다");

        mockQ1 = QuestionFlatResponse.builder()
            .questionId(1L)
            .content("질문1")
            .emojiCount(100L)
            .hasAnswer(false)
            .isEmojied(false)
            .memberId(1L)
            .nickname("테스트유저")
            .createdAt(LocalDateTime.now())
            .build();
        mockQ2 = QuestionFlatResponse.builder()
            .questionId(2L)
            .content("질문2")
            .emojiCount(1L)
            .hasAnswer(false)
            .isEmojied(false)
            .memberId(1L)
            .nickname("테스트유저")
            .createdAt(LocalDateTime.of(2000, 1, 1, 1, 1))
            .build();

    }

    @Test
    @DisplayName("질문 작성자이거나 관리자이면 질문이 성공적으로 삭제된다")
    void delete_sucess() {
        // given
        Long roomId = 1L;
        Long memberId = 1L;

        Question question = Question.builder().id(1L).room(mockRoom).member(mockMember)
            .content("질문").build();

        given(questionReader.getByIdAndRoomId(1L, roomId)).willReturn(question);
        given(participantReader.existsPresenterOrTeamByMemberId(roomId, memberId)).willReturn(true);
        doNothing().when(questionRepository).deleteByIdAndRoomId(1L, roomId);

        Long deleted = questionService.delete(memberId, roomId, 1L);

        assertThat(deleted).isEqualTo(1L);
        verify(questionRepository).deleteByIdAndRoomId(1L, roomId);
    }

    @Test
    @DisplayName("질문이 성공적으로 수정된다")
    void updateQuestion_success() {
        // given
        Long roomId = 1L;
        Long memberId = 1L;

        Question question = Question.builder().id(1L).room(mockRoom).member(mockMember).content("변경 전").build();

        given(questionReader.getByIdAndRoomId(1L, roomId)).willReturn(question);

        // when
        Question updated = questionService.update(memberId, roomId, 1L, request);

        // then
        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getContent()).isEqualTo("질문입니다");

    }

    @Test
    @DisplayName("잘못된 값이 들어오면 질문 수정이 실패한다")
    void updateQuestion_found_fail() {
        // given
        Long notRoomId = 999L;

        given(questionReader.getByIdAndRoomId(1L, notRoomId)).willThrow(
            new ErrorException(ErrorCode.NOT_FOUND_ROOM_QUESTION));

        // when & then
        assertThatThrownBy(() -> questionService.update(999L, notRoomId, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_ROOM_QUESTION);

    }

    @Test
    @DisplayName("작성자가 아니면 질문 수정이 실패한다")
    void updateQuestion_fail() {
        // given
        Long roomId = 1L;
        Long notMemberId = 999L;

        Question question = Question.builder().id(1L).room(mockRoom).member(mockMember).content("변경 전").build();

        given(questionReader.getByIdAndRoomId(1L, roomId)).willReturn(question);

        // when then
        assertThatThrownBy(() -> questionService.update(notMemberId, roomId, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_EDIT_QUESTION);

    }

    @Test
    @DisplayName("최신순 질문 목록 조회")
    void getQuestionByCreatedAt_success() {
        // given
        Long roomId = 1L;
        Long memberId = 1L;
        int size = 1;


        List<QuestionFlatResponse> mockList = List.of(mockQ1, mockQ2);

        given(memberReader.getById(memberId)).willReturn(mockMember);
        given(roomReader.getById(roomId)).willReturn(mockRoom);
        given(questionReader.findByCreatedAt(null, memberId, roomId, PageRequest.of(0, size + 1)))
            .willReturn(mockList);


        Slice<QuestionAssembleResponse> result = questionService.getQuestions(QuestionSort.CREATEDAT,
            null, null, size, memberId, roomId);

        assertThat(result).isNotNull();
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("공감순 질문 목록 조회")
    void getQuestionByEmoji_success() {
        // given
        Long roomId = 1L;
        Long memberId = 1L;
        int size = 1;

        List<QuestionFlatResponse> mockList = List.of(mockQ1, mockQ2);

        given(memberReader.getById(memberId)).willReturn(mockMember);
        given(roomReader.getById(roomId)).willReturn(mockRoom);
        given(questionReader.findByEmojiCount(null, null, memberId, roomId, PageRequest.of(0, size + 1)))
            .willReturn(mockList);


        Slice<QuestionAssembleResponse> result = questionService.getQuestions(QuestionSort.EMOJI,
            null, null, size, memberId, roomId);

        assertThat(result).isNotNull();
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("내 질문 목록 조회")
    void getQuestionByMyQuestion_success() {
        // given
        Long roomId = 1L;
        Long memberId = 1L;
        int size = 1;

        List<QuestionFlatResponse> mockList = List.of(mockQ1, mockQ2);

        given(memberReader.getById(memberId)).willReturn(mockMember);
        given(roomReader.getById(roomId)).willReturn(mockRoom);
        given(questionReader.findByMyQuestion(null, memberId, roomId, PageRequest.of(0, size + 1)))
            .willReturn(mockList);


        Slice<QuestionAssembleResponse> result = questionService.getQuestions(QuestionSort.MYQUESTION,
            null, null, size, memberId, roomId);

        assertThat(result).isNotNull();
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("정상적인 값이 들어오면 질문이 생성된다")
    void createQuestion_success() {
        // given
        Long roomId = 1L;
        Long memberId = 1L;

        Question question = Question.builder()
            .id(1L)
            .room(mockRoom)
            .member(mockMember)
            .emojiCount(0L)
            .version(1)
            .content("질문입니다")
            .build();

        given(memberReader.getById(memberId)).willReturn(mockMember);
        given(roomReader.getById(roomId)).willReturn(mockRoom);
        given(questionRepository.save(any(Question.class))).willReturn(question);

        // when
        Question result = questionService.create(roomId, memberId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRoom()).isEqualTo(mockRoom);
        assertThat(result.getMember()).isEqualTo(mockMember);
        assertThat(result.getContent()).isEqualTo("질문입니다");
    }

    @Test
    @DisplayName("존재하지 않는 member가 들어오면 예외 발생")
    void createQuestion_member_fail() {
        // given
        given(memberReader.getById(anyLong())).willThrow(new ErrorException(ErrorCode.NOT_FOUND_MEMBER));

        // when & then
        assertThatThrownBy(() -> questionService.create(1L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_MEMBER);

    }

    @Test
    @DisplayName("존재하지 않는 room이 들어오면 예외 발생")
    void createQuestion_room_fail() {
        // given
        given(memberReader.getById(anyLong())).willReturn(mockMember);
        given(roomReader.getById(anyLong())).willThrow(new ErrorException(ErrorCode.NOT_FOUND_ROOM));

        // when & then
        assertThatThrownBy(() -> questionService.create(1L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_ROOM);

    }

    @Test
    @DisplayName("존재하지 않는 participant가 들어오면 예외 발생")
    void createQuestion_participant_fail() {
        // given
        Long roomId = 1L;
        Long memberId = 1L;

        given(memberReader.getById(memberId)).willReturn(mockMember);
        given(roomReader.getById(roomId)).willReturn(mockRoom);
        willThrow(new ErrorException(ErrorCode.NOT_FOUND_PARTICIPANT))
                .given(participantService)
                .validateParticipant(roomId, memberId);


        // when & then
        assertThatThrownBy(() -> questionService.create(1L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_PARTICIPANT);

    }
}