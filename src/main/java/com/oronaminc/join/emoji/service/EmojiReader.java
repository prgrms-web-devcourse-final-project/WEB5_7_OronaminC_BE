package com.oronaminc.join.emoji.service;

import com.oronaminc.join.emoji.dao.EmojiRepository;
import com.oronaminc.join.emoji.domain.Emoji;
import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmojiReader {

    private final EmojiRepository emojiRepository;

    public Optional<Emoji> findByMemberIdAndTargetIdAndTargetType(Long memberId, Long targetId,
        TargetType targetType) {
        return emojiRepository.findByMemberIdAndTargetIdAndTargetType(memberId, targetId,
            targetType);
    }

    public Emoji findEmojiByMemberIdAndTargetIdAndTargetType(Long memberId, Long targetId,
        TargetType targetType) {
        return emojiRepository.findByMemberIdAndTargetIdAndTargetType(memberId, targetId,
                targetType)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_EMOJI));
    }

    public Integer countByTargetIdAndTargetType(Long targetId, TargetType targetType) {
        return emojiRepository.countByTargetIdAndTargetType(targetId, targetType);
    }

    public boolean existsByMemberIdAndTargetIdAndTargetType(Long memberId, Long targetId,
        TargetType targetType) {
        return emojiRepository.existsByMemberIdAndTargetIdAndTargetType(memberId, targetId,
            targetType);
    }

    public Set<Long> findTargetIdsByMemberAndTargetTypeInBatch(Long memberId, TargetType targetType, List<Long> targetIds) {
        if (targetIds.isEmpty() || targetType == null) return Set.of();
        return emojiRepository.findTargetIdsByMemberIdAndTargetTypeAndTargetIdIn(memberId, targetType, targetIds);
    }

}
