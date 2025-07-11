package com.oronaminc.join.emoji.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oronaminc.join.emoji.domain.Emoji;
import com.oronaminc.join.emoji.domain.TargetType;

public interface EmojiRepository extends JpaRepository<Emoji, Long> {
    Integer countByTargetIdAndTargetType(Long targetId, TargetType targetType);
    void deleteByTargetTypeAndTargetId(TargetType targetType, Long targetId);
}
