package com.oronaminc.join.question.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

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
import com.oronaminc.join.question.domain.QuestionSort;
import com.oronaminc.join.question.dto.QuestionCreateRequest;
import com.oronaminc.join.question.dto.QuestionFlatResponse;
import com.oronaminc.join.question.dto.QuestionAssembleResponse;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.domain.RoomStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class QuestionServiceTests {

    @InjectMocks
    private QuestionService questionService;

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ParticipantRepository participantRepository;

    private Room mockRoom;
    private Member mockMember;
    private Participant mockParticipant;
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

        mockParticipant = Participant.builder()
            .id(1L)
            .room(mockRoom)
            .member(mockMember)
            .participantType(ParticipantType.GUEST)
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
    @DisplayName("최신순 질문 목록 조회")
    void getQuestionByCreatedAt_success() {
        // given
        Long roomId = 1L;
        Long memberId = 1L;
        int size = 1;


        List<QuestionFlatResponse> mockList = List.of(mockQ1, mockQ2);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(mockMember));
        given(roomRepository.findById(roomId)).willReturn(Optional.of(mockRoom));
        given(questionRepository.findByCreatedAt(null, memberId, roomId, PageRequest.of(0, size + 1)))
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

        given(memberRepository.findById(memberId)).willReturn(Optional.of(mockMember));
        given(roomRepository.findById(roomId)).willReturn(Optional.of(mockRoom));
        given(questionRepository.findByEmojiCount(null, null, memberId, roomId, PageRequest.of(0, size + 1)))
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

        given(memberRepository.findById(memberId)).willReturn(Optional.of(mockMember));
        given(roomRepository.findById(roomId)).willReturn(Optional.of(mockRoom));
        given(questionRepository.findByMyQuestion(null, memberId, roomId, PageRequest.of(0, size + 1)))
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

        given(memberRepository.findById(memberId)).willReturn(Optional.of(mockMember));
        given(roomRepository.findById(roomId)).willReturn(Optional.of(mockRoom));
        given(participantRepository.existsByRoomIdAndMemberId(roomId, memberId)).willReturn(true);
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
        given(memberRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> questionService.create(1L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_MEMBER);

    }

    @Test
    @DisplayName("존재하지 않는 room이 들어오면 예외 발생")
    void createQuestion_room_fail() {
        // given
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(mockMember));
        given(roomRepository.findById(anyLong())).willReturn(Optional.empty());

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

        given(memberRepository.findById(memberId)).willReturn(Optional.of(mockMember));
        given(roomRepository.findById(roomId)).willReturn(Optional.of(mockRoom));
        given(participantRepository.existsByRoomIdAndMemberId(roomId, memberId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> questionService.create(1L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_PARTICIPANT);

    }
}