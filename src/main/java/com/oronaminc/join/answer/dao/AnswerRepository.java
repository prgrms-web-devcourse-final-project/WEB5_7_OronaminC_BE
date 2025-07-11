package com.oronaminc.join.answer.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.question.domain.Question;

public interface AnswerRepository extends JpaRepository<Answer,Long> {
    void deleteByQuestionIn(List<Question> questions);
}
