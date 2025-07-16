package com.oronaminc.join.websocket.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.global.ratelimit.RateLimitService;
import com.oronaminc.join.global.ratelimit.RateLimitType;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.dto.QuestionRequest;
import com.oronaminc.join.question.dto.QuestionCreateResponse;
import com.oronaminc.join.question.dto.QuestionDeleteResponse;
import com.oronaminc.join.question.dto.QuestionUpdateResponse;
import com.oronaminc.join.question.util.QuestionMapper;
import com.oronaminc.join.question.service.QuestionService;
import io.github.bucket4j.Bucket;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class QuestionWebsocketController {

    private final QuestionService questionService;
    private final RateLimitService rateLimitService;

    @MessageMapping("/rooms/{roomId}/questions/create")
    @SendTo("/topic/rooms/{roomId}/questions")
    public QuestionCreateResponse createQuestion(
        @DestinationVariable Long roomId,
        @Payload QuestionRequest request,
        Principal principal
    ) {
        Long memberId = Long.valueOf(principal.getName());

        Bucket bucket = rateLimitService.getBucket(RateLimitType.CREATE_QUESTION, roomId, memberId);

        if (!bucket.tryConsume(1)) {
            throw new ErrorException(ErrorCode.TOO_MANY_REQUESTS_QUESTION);
        }

        Question question = questionService.create(roomId, memberId, request);

        log.info("수신한 메시지 = {}", request.content());

        return QuestionMapper.toQuestionCreateResponse(question);
    }

    @MessageMapping("/rooms/{roomId}/questions/{questionId}/update")
    @SendTo("/topic/rooms/{roomId}/questions")
    public QuestionUpdateResponse updateQuestion(
        @DestinationVariable Long roomId,
        @DestinationVariable Long questionId,
        @Payload QuestionRequest request,
        Principal principal
    ) {
        Long memberId = Long.valueOf(principal.getName());

        Question updated = questionService.update(memberId, roomId, questionId, request);

        return QuestionMapper.toQuestionUpdateResponse(updated);
    }

    @MessageMapping("rooms/{roomId}/questions/{questionId}/delete")
    @SendTo("/topic/rooms/{roomId}/questions")
    public QuestionDeleteResponse deleteQuestion(
        @DestinationVariable Long roomId,
        @DestinationVariable Long questionId,
        Principal principal
    ) {
        Long memberId = Long.valueOf(principal.getName());

        Long deletedId = questionService.delete(memberId, roomId, questionId);

        return QuestionMapper.toQuestionDeleteResponse(deletedId);
    }
}
