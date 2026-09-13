package com.oronaminc.join.question.api;

import com.oronaminc.join.question.domain.QuestionSort;
import com.oronaminc.join.question.dto.QuestionAssembleResponse;
import com.oronaminc.join.question.dto.QuestionListResponse;
import com.oronaminc.join.question.service.QuestionService;
import com.oronaminc.join.question.util.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/rooms/{roomId}/questions")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QuestionListResponse> getQuestions(
        @RequestParam QuestionSort sort,
        @RequestParam(required = false) Long lastId,
        @RequestParam(required = false) Long lastEmojiCount,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam Long memberId,
        @PathVariable Long roomId
    ) {
        Slice<QuestionAssembleResponse> result = questionService.getQuestions(
            sort, lastId, lastEmojiCount, size, memberId, roomId
        );
        return ResponseEntity.ok(QuestionMapper.toQuestionListResponse(result));
    }
}
