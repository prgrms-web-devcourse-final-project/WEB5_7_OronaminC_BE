package com.oronaminc.join.answer.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.global.exception.ErrorException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AnswerReader {
    private final AnswerRepository answerRepository;

    public boolean existsByQuestionIdAndMemberId(Long questionId, Long memberId) {
        return answerRepository.existsByQuestionIdAndMemberId(questionId, memberId);
    }

    public Optional<Answer> findById(Long answerId) {
        return answerRepository.findById(answerId);
    }

    public Answer getById(Long answerId) {
        return findById(answerId)
                .orElseThrow(() -> new ErrorException(NOT_FOUND_ANSWER));
    }
}
