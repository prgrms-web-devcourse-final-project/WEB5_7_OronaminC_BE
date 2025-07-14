package com.oronaminc.join.answer.dao;

import java.util.List;

import com.oronaminc.join.room.dto.TopQnADto;
import org.springframework.data.jpa.repository.JpaRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.question.domain.Question;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    boolean existsByQuestionIdAndMemberId(Long questionId, Long memberId);

    void deleteByQuestionIn(List<Question> questions);

    @Query("""
        select count(a)
        from Answer a
        where a.question.room.id = :roomId
    """)
    Long countAnsweredQuestionsByRoomId(@Param("roomId") Long roomId);

}
