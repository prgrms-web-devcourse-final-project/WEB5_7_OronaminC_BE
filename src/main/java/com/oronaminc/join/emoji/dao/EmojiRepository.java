package com.oronaminc.join.emoji.dao;

import com.oronaminc.join.emoji.domain.Emoji;
import com.oronaminc.join.emoji.domain.TargetType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmojiRepository extends JpaRepository<Emoji, String> {

    Integer countByTargetIdAndTargetType(Long targetId, TargetType targetType);

    void deleteByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    Optional<Emoji> findByMemberIdAndTargetIdAndTargetType(Long memberId, Long targetId,
        TargetType targetType);

}
