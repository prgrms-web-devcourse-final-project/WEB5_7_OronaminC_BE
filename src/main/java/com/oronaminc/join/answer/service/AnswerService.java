package com.oronaminc.join.answer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.question.domain.Question;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;

    public void deleteByQuestionList(List<Question> questions) {
        answerRepository.deleteByQuestionIn(questions);
    }

    public Answer findById(Long id) {
        return answerRepository.findById(id)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_ROOM));
    }
}
