package com.oronaminc.join.answer.service;

import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnswerReader {

    private final AnswerRepository answerRepository;

    public Optional<Answer> findById(Long answerId) {
        return answerRepository.findById(answerId);
    }

    public List<Answer> getFirstPageByQuestionId(Long questionId, Pageable pageable) {
        return answerRepository.findFirstPageByQuestionId(questionId, pageable);
    }

    public List<Answer> getAnswerByQuestionIdWithCursor(Long questionId,
        LocalDateTime lastCreatedAt, Long lastId, Pageable pageable) {
        return answerRepository.findByQuestionIdWithCursor(questionId, lastCreatedAt, lastId,
            pageable);
    }

    public Answer getByQuestionId(Long questionId) {
        return answerRepository.findByQuestionId(questionId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_EXIST_ANSWER));
    }

    public Answer getById(Long answerId) {
        return findById(answerId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_ANSWER));
    }

    public Long countAnsweredQuestionsByRoomId(Long roomId) {
        return answerRepository.countAnsweredQuestionsByRoomId(roomId);
    }

    public List<Answer> getAnswerByQuestionIds(List<Long> questionIds) {
        return answerRepository.findAllByQuestionIds(questionIds);
    }
}
