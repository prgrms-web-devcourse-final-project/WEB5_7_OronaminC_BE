package com.oronaminc.join.emoji.dao;

import com.oronaminc.join.emoji.domain.Emoji;
import com.oronaminc.join.emoji.domain.TargetType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmojiRepository extends JpaRepository<Emoji, String> {

    void deleteByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    Optional<Emoji> findByMemberIdAndTargetIdAndTargetType(Long memberId, Long targetId,
        TargetType targetType);

    Integer countByTargetIdAndTargetType(Long targetId, TargetType targetType);

    boolean existsByMemberIdAndTargetIdAndTargetType(Long memberId, Long targetId,
        TargetType targetType);

    @Query("""
            SELECT e.targetId
            FROM Emoji e
            WHERE e.member.id = :memberId
              AND e.targetType = :targetType
              AND e.targetId IN :targetIds
        """)
    Set<Long> findTargetIdsByMemberIdAndTargetTypeAndTargetIdIn(
        @Param("memberId") Long memberId,
        @Param("targetType") TargetType targetType,
        @Param("targetIds") List<Long> targetIds
    );
}
