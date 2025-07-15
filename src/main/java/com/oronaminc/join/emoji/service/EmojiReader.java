package com.oronaminc.join.emoji.service;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.oronaminc.join.emoji.dao.EmojiRepository;
import com.oronaminc.join.emoji.domain.Emoji;
import com.oronaminc.join.emoji.domain.TargetType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmojiReader {
    private final EmojiRepository emojiRepository;

    public Optional<Emoji> findByMemberIdAndTargetIdAndTargetType(Long memberId, Long targetId, TargetType targetType) {
        return emojiRepository.findByMemberIdAndTargetIdAndTargetType(memberId, targetId, targetType);
    }

    public Integer countByTargetIdAndTargetType(Long targetId, TargetType targetType) {
        return emojiRepository.countByTargetIdAndTargetType(targetId, targetType);
    }

}
