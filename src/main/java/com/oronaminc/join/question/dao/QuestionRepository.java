package com.oronaminc.join.question.dao;

import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.dto.QuestionListResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("""
        SELECT new com.oronaminc.join.question.dto.QuestionListResponse(
            q.id,
            q.content,
            q.emojiCount,
            (CASE WHEN EXISTS (
                SELECT a FROM Answer a 
                WHERE a.question = q
            ) THEN true ELSE false END),
            (CASE WHEN EXISTS (
                SELECT e FROM Emoji e 
                WHERE e.member.id = :memberId 
                AND e.targetType = com.oronaminc.join.emoji.domain.TargetType.QUESTION
                AND e.targetId = q.id
            ) THEN true ELSE false END),
             m.id,
             m.nickname,
             q.createdAt
        )
        FROM Question q
        JOIN q.member m
        WHERE (:lastId IS NULL OR q.id < :lastId)
        ORDER BY q.id ASC
    """)
    List<QuestionListResponse> findByCreatedAt(@Param("cursor") Long lastId,
        @Param("memberId") Long memberId, Pageable pageable);

    @Query("""
        SELECT new com.oronaminc.join.question.dto.QuestionListResponse(
            q.id,
            q.content,
            q.emojiCount,
            (CASE WHEN EXISTS (
                SELECT a FROM Answer a 
                WHERE a.question = q
            ) THEN true ELSE false END),
            (CASE WHEN EXISTS (
                SELECT e FROM Emoji e 
                WHERE e.member.id = :memberId 
                AND e.targetType = com.oronaminc.join.emoji.domain.TargetType.QUESTION
                AND e.targetId = q.id
            ) THEN true ELSE false END),
             m.id,
             m.nickname,
             q.createdAt
        )
        FROM Question q
        JOIN q.member m
        WHERE (:lastEmojiCount IS NULL OR (
            q.emojiCount < :lastEmojiCount OR (q.emojiCount = :lastEmojiCount AND q.id < :lastId)
        ))
        ORDER BY q.emojiCount DESC, q.id DESC 
    """)
    List<QuestionListResponse> findByEmojiCount(@Param("cursor") Long lastId,
        @Param("lastEmojiCount") Long lastEmojiCount,
        @Param("memberId") Long memberId, Pageable pageable);

    @Query("""
        SELECT new com.oronaminc.join.question.dto.QuestionListResponse(
            q.id,
            q.content,
            q.emojiCount,
            (CASE WHEN EXISTS (
                SELECT a FROM Answer a 
                WHERE a.question = q
            ) THEN true ELSE false END),
            (CASE WHEN EXISTS (
                SELECT e FROM Emoji e 
                WHERE e.member.id = :memberId 
                AND e.targetType = com.oronaminc.join.emoji.domain.TargetType.QUESTION
                AND e.targetId = q.id
            ) THEN true ELSE false END),
             m.id,
             m.nickname,
             q.createdAt
        )
        FROM Question q
        JOIN q.member m
        WHERE q.member.id = :memberId
        AND (:lastId IS NULL OR  q.id < :lastId)
        ORDER BY q.id DESC 
    """)
    List<QuestionListResponse> findByMyQuestion(@Param("cursor") Long lastId,
        @Param("memberId") Long memberId, Pageable pageable);

}