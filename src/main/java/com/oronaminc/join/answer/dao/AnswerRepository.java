package com.oronaminc.join.answer.dao;

import java.util.List;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.question.domain.Question;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    Optional<Answer> findByQuestionId(Long questionId);

    boolean existsByQuestionIdAndMemberId(Long questionId, Long memberId);

    void deleteByQuestionId(Long questionId);

    void deleteByQuestionIn(List<Question> questions);
}
