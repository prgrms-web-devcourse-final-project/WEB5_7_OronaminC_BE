package com.oronaminc.join.answer.dao;

import com.oronaminc.join.answer.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

}
