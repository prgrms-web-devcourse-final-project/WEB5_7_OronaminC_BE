package com.oronaminc.join.emoji.service;

import com.oronaminc.join.answer.service.AnswerService;
import com.oronaminc.join.emoji.dao.EmojiRepository;
import com.oronaminc.join.emoji.domain.Emoji;
import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.emoji.dto.EmojiRequest;
import com.oronaminc.join.emoji.dto.EmojiResponse;
import com.oronaminc.join.member.service.MemberService;
import com.oronaminc.join.question.service.QuestionService;
import com.oronaminc.join.room.service.RoomService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmojiService {

    private final EmojiRepository emojiRepository;
    private final RoomService roomService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final MemberService memberService;

    @Transactional
    public EmojiResponse toggleEmoji(Long memberId, EmojiRequest emojiRequest) {
        Long emojiCount;
        TargetType targetType = emojiRequest.targetType();
        Long targetId = emojiRequest.targetId();

        Optional<Emoji> findEmoji = emojiRepository.findByMemberIdAndTargetIdAndTargetType(
            memberId, targetId, targetType);

        if (findEmoji.isPresent()) {
            emojiRepository.delete(findEmoji.get());
            emojiCount = decrementEmojiCount(targetType, targetId);

            return new EmojiResponse("DELETE", targetType, targetId, emojiCount);

        } else {
            Emoji emoji = Emoji.builder()
                .member(memberService.getMember(memberId))
                .targetType(targetType)
                .targetId(targetId)
                .build();
            emojiRepository.save(emoji);

            emojiCount = incrementEmojiCount(targetType, targetId);

            return new EmojiResponse("CREATE", targetType, targetId, emojiCount);
        }
    }

    private Long decrementEmojiCount(TargetType targetType, Long targetId) {
        return switch (targetType) {
            case ROOM -> roomService.findById(targetId).decrementEmojiCount();
            case QUESTION -> questionService.findById(targetId).decrementEmojiCount();
            case ANSWER -> answerService.findById(targetId).decrementEmojiCount();
        };
    }

    private Long incrementEmojiCount(TargetType targetType, Long targetId) {
        return switch (targetType) {
            case ROOM -> roomService.findById(targetId).incrementEmojiCount();
            case QUESTION -> questionService.findById(targetId).incrementEmojiCount();
            case ANSWER -> answerService.findById(targetId).incrementEmojiCount();
        };
    }

}
