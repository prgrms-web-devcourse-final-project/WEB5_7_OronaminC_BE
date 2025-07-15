package com.oronaminc.join.question.service;

import java.util.List;
import java.util.Optional;

import com.oronaminc.join.room.dto.TopQnADto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.question.dao.QuestionRepository;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.dto.QuestionFlatResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QuestionReader {
    private final QuestionRepository questionRepository;

    public Optional<Question> findById(Long questionId) {
        return questionRepository.findById(questionId);
    }

    public Question getById(Long questionId) {
        return this.findById(questionId)
                .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_QUESTION));
    }

    public Optional<Question> findByIdAndRoomId(Long questionId, Long roomId) {
        return questionRepository.findByIdAndRoomId(questionId, roomId);
    }

    public Question getByIdAndRoomId(Long questionId, Long roomId) {
        return this.findByIdAndRoomId(questionId, roomId)
                .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_ROOM_QUESTION));
    }

    public List<QuestionFlatResponse> findByCreatedAt(Long lastId, Long memberId, Long roomId, Pageable pageable) {
        return questionRepository.findByCreatedAt(lastId, memberId, roomId, pageable);
    }

    public List<QuestionFlatResponse> findByEmojiCount(Long lastId, Long lastEmojiCount, Long memberId, Long roomId, Pageable pageable) {
        return questionRepository.findByEmojiCount(lastId, lastEmojiCount, memberId, roomId, pageable);
    }

    public List<QuestionFlatResponse> findByMyQuestion(Long lastId, Long memberId, Long roomId, Pageable pageable) {
        return questionRepository.findByMyQuestion(lastId, memberId, roomId, pageable);
    }

    public List<Question> findByRoomId(Long roomId) {
        return questionRepository.findByRoomId(roomId);
    }

    public List<Object[]> countByRoomIds(List<Long> roomIds) {
        return questionRepository.countByRoomIds(roomIds);
    }

    public boolean existsInRoom(Long roomId) {
        return !questionRepository.findByRoomId(roomId).isEmpty();
    }

    public Long countByRoomId(Long roomId) { return questionRepository.countByRoomId(roomId);}

    public List<TopQnADto> findTop3QnA(Long roomId) {
        return questionRepository.findTop3QnAByRoomId(roomId, PageRequest.of(0,3));
    }
}
