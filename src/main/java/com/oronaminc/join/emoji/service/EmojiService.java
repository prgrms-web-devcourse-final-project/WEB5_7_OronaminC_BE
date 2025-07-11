package com.oronaminc.join.emoji.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.answer.service.AnswerService;
import com.oronaminc.join.emoji.dao.EmojiRepository;
import com.oronaminc.join.emoji.domain.Emoji;
import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.emoji.dto.EmojiRequest;
import com.oronaminc.join.emoji.dto.EmojiResponse;
import com.oronaminc.join.member.service.MemberService;
import com.oronaminc.join.question.service.QuestionReader;
import com.oronaminc.join.room.service.RoomReader;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmojiService {

    private final EmojiRepository emojiRepository;
    private final AnswerService answerService;
    private final MemberService memberService;
    private final RoomReader roomReader;
    private final QuestionReader questionReader;

    @Transactional
    public void deleteByRoomEmoji(Long roomId) {
        emojiRepository.deleteByTargetTypeAndTargetId(TargetType.ROOM, roomId);
    }

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
        }

        Emoji emoji = Emoji.create(memberService.findById(memberId), targetType, targetId);
        emojiRepository.save(emoji);

        emojiCount = incrementEmojiCount(targetType, targetId);

        return new EmojiResponse("CREATE", targetType, targetId, emojiCount);

    }

    private Long decrementEmojiCount(TargetType targetType, Long targetId) {
        return switch (targetType) {
            case ROOM -> roomReader.getById(targetId).decrementEmojiCount();
            case QUESTION -> questionReader.getById(targetId).decrementEmojiCount();
            case ANSWER -> answerService.findById(targetId).decrementEmojiCount();
        };
    }

    private Long incrementEmojiCount(TargetType targetType, Long targetId) {
        return switch (targetType) {
            case ROOM -> roomReader.getById(targetId).incrementEmojiCount();
            case QUESTION -> questionReader.getById(targetId).incrementEmojiCount();
            case ANSWER -> answerService.findById(targetId).incrementEmojiCount();
        };
    }

}
