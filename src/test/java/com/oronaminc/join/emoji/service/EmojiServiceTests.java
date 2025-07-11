package com.oronaminc.join.emoji.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.service.AnswerService;
import com.oronaminc.join.emoji.dao.EmojiRepository;
import com.oronaminc.join.emoji.domain.Emoji;
import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.emoji.dto.EmojiRequest;
import com.oronaminc.join.emoji.dto.EmojiResponse;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.service.MemberService;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.service.QuestionService;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmojiServiceTests {

    @Mock
    private EmojiRepository emojiRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private QuestionService questionService;

    @Mock
    private AnswerService answerService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private EmojiService emojiService;

    @Test
    @DisplayName("멤버가 발표방 좋아요를 누르지 않은 상태에서 toggle 시 좋아요 수가 +1 된다")
    void toggleEmoji_createRoomEmoji_success() {
        // given
        Member member = Member.builder().build();
        Long memberId = member.getId();

        TargetType targetType = TargetType.ROOM;
        Long targetId = 100L;
        Long emojiCount = 1L;

        Room room = Room.builder().emojiCount(emojiCount).build();
        ReflectionTestUtils.setField(room, "id", targetId);

        Emoji findEmoji = Emoji.builder()
            .member(member)
            .targetType(targetType)
            .targetId(targetId)
            .build();

        when(emojiRepository.findByMemberIdAndTargetIdAndTargetType(memberId, targetId,
            targetType)).thenReturn(Optional.empty());
        when(memberService.findById(memberId)).thenReturn(member);
        when(emojiRepository.save(any(Emoji.class))).thenReturn(findEmoji);
        when(roomRepository.findById(targetId)).thenReturn(Optional.of(room));

        // when
        EmojiResponse response = emojiService.toggleEmoji(memberId,
            new EmojiRequest(targetType, targetId));

        // then
        assertThat(response.event()).isEqualTo("CREATE");
        assertThat(response.targetType()).isEqualTo(targetType);
        assertThat(response.targetId()).isEqualTo(targetId);
        assertThat(response.emojiCount()).isEqualTo(emojiCount + 1);

    }

    @Test
    @DisplayName("멤버가 질문 공감을 누르지 않은 상태에서 toggle 시 공감 수가 +1 된다")
    void toggleEmoji_createQuestionEmoji_success() {
        // given
        Member member = Member.builder().build();
        Long memberId = member.getId();

        TargetType targetType = TargetType.QUESTION;
        Long targetId = 100L;
        Long emojiCount = 1L;

        Question question = Question.builder().emojiCount(emojiCount).build();
        ReflectionTestUtils.setField(question, "id", targetId);

        Emoji findEmoji = Emoji.builder()
            .member(member)
            .targetType(targetType)
            .targetId(targetId)
            .build();

        when(emojiRepository.findByMemberIdAndTargetIdAndTargetType(memberId, targetId,
            targetType)).thenReturn(Optional.empty());
        when(memberService.findById(memberId)).thenReturn(member);
        when(emojiRepository.save(any(Emoji.class))).thenReturn(findEmoji);
        when(questionService.findById(targetId)).thenReturn(question);

        // when
        EmojiResponse response = emojiService.toggleEmoji(memberId,
            new EmojiRequest(targetType, targetId));

        // then
        assertThat(response.event()).isEqualTo("CREATE");
        assertThat(response.targetType()).isEqualTo(targetType);
        assertThat(response.targetId()).isEqualTo(targetId);
        assertThat(response.emojiCount()).isEqualTo(emojiCount + 1);

    }

    @Test
    @DisplayName("멤버가 답변 공감을 누르지 않은 상태에서 toggle 시 공감 수가 +1 된다")
    void toggleEmoji_createAnswerEmoji_success() {
        // given
        Member member = Member.builder().build();
        Long memberId = member.getId();

        TargetType targetType = TargetType.ANSWER;
        Long targetId = 100L;
        Long emojiCount = 1L;

        Answer answer = Answer.builder().emojiCount(emojiCount).build();
        ReflectionTestUtils.setField(answer, "id", targetId);

        Emoji findEmoji = Emoji.builder()
            .member(member)
            .targetType(targetType)
            .targetId(targetId)
            .build();

        when(emojiRepository.findByMemberIdAndTargetIdAndTargetType(memberId, targetId,
            targetType)).thenReturn(Optional.empty());
        when(memberService.findById(memberId)).thenReturn(member);
        when(emojiRepository.save(any(Emoji.class))).thenReturn(findEmoji);
        when(answerService.findById(targetId)).thenReturn(answer);

        // when
        EmojiResponse response = emojiService.toggleEmoji(memberId,
            new EmojiRequest(targetType, targetId));

        // then
        assertThat(response.event()).isEqualTo("CREATE");
        assertThat(response.targetType()).isEqualTo(targetType);
        assertThat(response.targetId()).isEqualTo(targetId);
        assertThat(response.emojiCount()).isEqualTo(emojiCount + 1);

    }

    @Test
    @DisplayName("멤버가 발표방 좋아요를 누른 상태에서 toggle 시 좋아요 수가 -1 된다")
    void toggleEmoji_deleteRoomEmoji_success() {
        // given
        Member member = Member.builder().build();
        Long memberId = member.getId();

        TargetType targetType = TargetType.ROOM;
        Long targetId = 100L;
        Long emojiCount = 3L;

        Room room = Room.builder().emojiCount(emojiCount).build();
        ReflectionTestUtils.setField(room, "id", targetId);

        Emoji findEmoji = Emoji.builder()
            .member(member)
            .targetType(targetType)
            .targetId(targetId)
            .build();

        when(emojiRepository.findByMemberIdAndTargetIdAndTargetType(memberId, targetId,
            targetType)).thenReturn(Optional.of(findEmoji));
        when(roomRepository.findById(targetId)).thenReturn(Optional.of(room));

        // when
        EmojiResponse response = emojiService.toggleEmoji(memberId,
            new EmojiRequest(targetType, targetId));

        // then
        assertThat(response.event()).isEqualTo("DELETE");
        assertThat(response.targetType()).isEqualTo(targetType);
        assertThat(response.targetId()).isEqualTo(targetId);
        assertThat(response.emojiCount()).isEqualTo(emojiCount - 1);

    }

    @Test
    @DisplayName("멤버가 질문 공감을 누른 상태에서 toggle 시 공감 수가 -1 된다")
    void toggleEmoji_deleteQuestionEmoji_success() {
        // given
        Member member = Member.builder().build();
        Long memberId = member.getId();

        TargetType targetType = TargetType.QUESTION;
        Long targetId = 100L;
        Long emojiCount = 3L;

        Question question = Question.builder().emojiCount(emojiCount).build();
        ReflectionTestUtils.setField(question, "id", targetId);

        Emoji findEmoji = Emoji.builder()
            .member(member)
            .targetType(targetType)
            .targetId(targetId)
            .build();

        when(emojiRepository.findByMemberIdAndTargetIdAndTargetType(memberId, targetId,
            targetType)).thenReturn(Optional.of(findEmoji));
        when(questionService.findById(targetId)).thenReturn(question);

        // when
        EmojiResponse response = emojiService.toggleEmoji(memberId,
            new EmojiRequest(targetType, targetId));

        // then
        assertThat(response.event()).isEqualTo("DELETE");
        assertThat(response.targetType()).isEqualTo(targetType);
        assertThat(response.targetId()).isEqualTo(targetId);
        assertThat(response.emojiCount()).isEqualTo(emojiCount - 1);

    }

    @Test
    @DisplayName("멤버가 발표방 좋아요를 누른 상태에서 toggle 시 좋아요 수가 -1 된다")
    void toggleEmoji_deleteAnswerEmoji_success() {
        // given
        Member member = Member.builder().build();
        Long memberId = member.getId();

        TargetType targetType = TargetType.ANSWER;
        Long targetId = 100L;
        Long emojiCount = 3L;

        Answer answer = Answer.builder().emojiCount(emojiCount).build();
        ReflectionTestUtils.setField(answer, "id", targetId);

        Emoji findEmoji = Emoji.builder()
            .member(member)
            .targetType(targetType)
            .targetId(targetId)
            .build();

        when(emojiRepository.findByMemberIdAndTargetIdAndTargetType(memberId, targetId,
            targetType)).thenReturn(Optional.of(findEmoji));
        when(answerService.findById(targetId)).thenReturn(answer);

        // when
        EmojiResponse response = emojiService.toggleEmoji(memberId,
            new EmojiRequest(targetType, targetId));

        // then
        assertThat(response.event()).isEqualTo("DELETE");
        assertThat(response.targetType()).isEqualTo(targetType);
        assertThat(response.targetId()).isEqualTo(targetId);
        assertThat(response.emojiCount()).isEqualTo(emojiCount - 1);

    }


}