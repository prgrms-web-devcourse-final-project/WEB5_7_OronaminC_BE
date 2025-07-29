package com.oronaminc.join.answer.dao;

import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.question.domain.Question;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    Optional<Answer> findByQuestionId(Long questionId);

    @Query("""
            SELECT a
            FROM Answer a
            JOIN FETCH a.member m
            WHERE a.question.id = :questionId
            ORDER BY a.createdAt DESC, a.id DESC
        """)
    List<Answer> findFirstPageByQuestionId(
        @Param("questionId") Long questionId,
        Pageable pageable
    );

    @Query("""
            SELECT a
            FROM Answer a
            JOIN FETCH a.member m
            WHERE a.question.id = :questionId
              AND (a.createdAt < :lastCreatedAt OR (a.createdAt = :lastCreatedAt AND a.id < :lastId))
            ORDER BY a.createdAt DESC, a.id DESC
        """)
    List<Answer> findByQuestionIdWithCursor(
        @Param("questionId") Long questionId,
        @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
        @Param("lastId") Long lastId,
        Pageable pageable
    );

    void deleteByQuestionId(Long questionId);

    void deleteByQuestionIn(List<Question> questions);

    @Query("""
            select count(distinct a.question.id)
            from Answer a
            where a.question.room.id = :roomId
        """)
    Long countAnsweredQuestionsByRoomId(@Param("roomId") Long roomId);

    @Query("""
            select a
            from Answer a
            where a.question.id in :questionIds
        """)
    List<Answer> findAllByQuestionIds(@Param("questionIds") List<Long> questionIds);

}
