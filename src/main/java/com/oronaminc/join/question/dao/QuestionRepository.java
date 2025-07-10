package com.oronaminc.join.question.dao;

import com.oronaminc.join.question.domain.Question;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    Optional<Question> findByIdAndRoomId(Long questionId, Long roomId);

}
