package com.oronaminc.join.answer.dao;


import java.util.List;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.question.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

   boolean existsByQuestionIdAndMemberId(Long questionId, Long memberId);
  
   void deleteByQuestionIn(List<Question> questions);

}
