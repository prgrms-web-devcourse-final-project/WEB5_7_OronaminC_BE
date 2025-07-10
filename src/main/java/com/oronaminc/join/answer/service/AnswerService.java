package com.oronaminc.join.answer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.question.domain.Question;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;

    public void deleteByQuestionList(List<Question> questions) {
        answerRepository.deleteByQuestionIn(questions);
    }
}
