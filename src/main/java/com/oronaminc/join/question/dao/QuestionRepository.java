package com.oronaminc.join.question.dao;

import com.oronaminc.join.question.domain.Question;
import java.util.Optional;
import com.oronaminc.join.question.dto.QuestionFlatResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    Optional<Question> findByIdAndRoomId(Long questionId, Long roomId);

    @Query("""
        SELECT new com.oronaminc.join.question.dto.QuestionFlatResponse(
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
        WHERE :roomId = q.room.id
        AND (:lastId IS NULL OR q.id < :lastId)
        ORDER BY q.id DESC 
    """)
    List<QuestionFlatResponse> findByCreatedAt(@Param("lastId") Long lastId,
        @Param("memberId") Long memberId, @Param("roomId") Long roomId, Pageable pageable);

    @Query("""
        SELECT new com.oronaminc.join.question.dto.QuestionFlatResponse(
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
        WHERE :roomId = q.room.id
        AND (:lastEmojiCount IS NULL OR (
            q.emojiCount < :lastEmojiCount OR (q.emojiCount = :lastEmojiCount AND q.id < :lastId)
        ))
        ORDER BY q.emojiCount DESC, q.id DESC 
    """)
    List<QuestionFlatResponse> findByEmojiCount(@Param("lastId") Long lastId,
        @Param("lastEmojiCount") Long lastEmojiCount,
        @Param("memberId") Long memberId, @Param("roomId") Long roomId, Pageable pageable);

    @Query("""
        SELECT new com.oronaminc.join.question.dto.QuestionFlatResponse(
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
        WHERE :roomId = q.room.id
        AND  q.member.id = :memberId
        AND (:lastId IS NULL OR  q.id > :lastId)
        ORDER BY q.id DESC 
    """)
    List<QuestionFlatResponse> findByMyQuestion(@Param("lastId") Long lastId,
        @Param("memberId") Long memberId, @Param("roomId") Long roomId, Pageable pageable);

    @Query("""
        select q.room.id, count(q)
        from Question q
        where q.room.id  in (:roomIds)
        group by q.room.id
    """)
    List<Object[]> countByRoomIds(List<Long> roomIds);

    List<Question> findByRoomId(Long roomId);

    void deleteByRoomId(Long roomId);
}
