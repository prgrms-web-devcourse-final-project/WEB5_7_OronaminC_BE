package com.oronaminc.join.question.repository;

import com.oronaminc.join.question.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {


}
