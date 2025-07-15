package com.oronaminc.join.emoji.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oronaminc.join.emoji.domain.Emoji;
import com.oronaminc.join.emoji.domain.TargetType;

public interface EmojiRepository extends JpaRepository<Emoji, String> {

    void deleteByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    Optional<Emoji> findByMemberIdAndTargetIdAndTargetType(Long memberId, Long targetId,
        TargetType targetType);

    Integer countByTargetIdAndTargetType(Long targetId, TargetType targetType);
}
