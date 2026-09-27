package com.oronaminc.join.emoji.service;

import com.oronaminc.join.answer.service.AnswerReader;
import com.oronaminc.join.emoji.dao.EmojiRepository;
import com.oronaminc.join.emoji.domain.Emoji;
import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.emoji.dto.EmojiRequest;
import com.oronaminc.join.emoji.dto.EmojiResponse;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.service.MemberReader;
import com.oronaminc.join.question.service.QuestionReader;
import com.oronaminc.join.room.service.RoomReader;
import com.oronaminc.join.websocket.common.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmojiService {

    private final EmojiRepository emojiRepository;
    private final QuestionReader questionReader;
    private final AnswerReader answerReader;
    private final MemberReader memberReader;
    private final EmojiReader emojiReader;
    private final RoomReader roomReader;

    @Transactional
    public void deleteByRoomEmoji(Long roomId) {
        emojiRepository.deleteByTargetTypeAndTargetId(TargetType.ROOM, roomId);
    }

    @Transactional
    public EmojiResponse createEmoji(Long memberId, EmojiRequest emojiRequest) {
        Long emojiCount;
        TargetType targetType = emojiRequest.targetType();
        Long targetId = emojiRequest.targetId();

        if (emojiReader.existsByMemberIdAndTargetIdAndTargetType(memberId, targetId, targetType)) {
            throw new ErrorException(ErrorCode.ALREADY_EXISTS_EMOJI);
        }

        emojiRepository.save(Emoji.create(memberReader.getById(memberId), targetType, targetId));

        emojiCount = incrementEmojiCount(targetType, targetId);

        return new EmojiResponse(EventType.CREATE, targetType, targetId, emojiCount);

    }

    @Transactional
    public EmojiResponse deleteEmoji(Long memberId, EmojiRequest emojiRequest) {
        Long emojiCount;
        TargetType targetType = emojiRequest.targetType();
        Long targetId = emojiRequest.targetId();

        emojiRepository.delete(
            emojiReader.findEmojiByMemberIdAndTargetIdAndTargetType(memberId, targetId,
                targetType)
        );
        emojiCount = decrementEmojiCount(targetType, targetId);

        return new EmojiResponse(EventType.DELETE, targetType, targetId, emojiCount);

    }

    private Long decrementEmojiCount(TargetType targetType, Long targetId) {
        return switch (targetType) {
            case ROOM -> roomReader.getById(targetId).decrementEmojiCount();
            case QUESTION -> questionReader.getById(targetId).decrementEmojiCount();
            case ANSWER -> answerReader.getById(targetId).decrementEmojiCount();
        };
    }

    private Long incrementEmojiCount(TargetType targetType, Long targetId) {
        return switch (targetType) {
            case ROOM -> roomReader.getById(targetId).incrementEmojiCount();
            case QUESTION -> questionReader.getById(targetId).incrementEmojiCount();
            case ANSWER -> answerReader.getById(targetId).incrementEmojiCount();
        };
    }

    private Long getEmojiCount(TargetType targetType, Long targetId) {
        return switch (targetType) {
            case ROOM -> roomReader.getById(targetId).getEmojiCount();
            case QUESTION -> questionReader.getById(targetId).getEmojiCount();
            case ANSWER -> answerReader.getById(targetId).getEmojiCount();
        };
    }

}
