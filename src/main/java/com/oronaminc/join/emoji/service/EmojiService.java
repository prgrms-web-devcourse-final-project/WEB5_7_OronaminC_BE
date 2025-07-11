package com.oronaminc.join.emoji.service;

import static com.oronaminc.join.global.exception.ErrorCode.NOT_FOUND_ROOM;

import com.oronaminc.join.answer.service.AnswerService;
import com.oronaminc.join.emoji.dao.EmojiRepository;
import com.oronaminc.join.emoji.domain.Emoji;
import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.emoji.dto.EmojiRequest;
import com.oronaminc.join.emoji.dto.EmojiResponse;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.service.MemberService;
import com.oronaminc.join.question.service.QuestionService;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmojiService {

    private final EmojiRepository emojiRepository;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final MemberService memberService;
    private final RoomRepository roomRepository;

    public Integer countRoomEmoji(Long roomId) {
        return emojiRepository.countByTargetIdAndTargetType(roomId, TargetType.ROOM);
    }

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

        } else {
            Emoji emoji = Emoji.create(memberService.findById(memberId), targetType, targetId);
            emojiRepository.save(emoji);

            emojiCount = incrementEmojiCount(targetType, targetId);

            return new EmojiResponse("CREATE", targetType, targetId, emojiCount);
        }
    }

    private Long decrementEmojiCount(TargetType targetType, Long targetId) {
        return switch (targetType) {
            case ROOM -> getRoomById(targetId).decrementEmojiCount();
            case QUESTION -> questionService.findById(targetId).decrementEmojiCount();
            case ANSWER -> answerService.findById(targetId).decrementEmojiCount();
        };
    }

    private Long incrementEmojiCount(TargetType targetType, Long targetId) {
        return switch (targetType) {
            case ROOM -> getRoomById(targetId).incrementEmojiCount();
            case QUESTION -> questionService.findById(targetId).incrementEmojiCount();
            case ANSWER -> answerService.findById(targetId).incrementEmojiCount();
        };
    }

    private Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
            .orElseThrow(() -> new ErrorException(NOT_FOUND_ROOM));
    }

}
