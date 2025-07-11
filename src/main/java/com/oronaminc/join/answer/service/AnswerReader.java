package com.oronaminc.join.answer.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.room.domain.Room;
import java.util.List;
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

    public Answer getExistById(Long questionId) {
        return answerRepository.findByQuestionId(questionId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_EXIST_ANSWER));
    }

    public Answer getById(Long answerId) {
        return findById(answerId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_ANSWER));
    }
}
