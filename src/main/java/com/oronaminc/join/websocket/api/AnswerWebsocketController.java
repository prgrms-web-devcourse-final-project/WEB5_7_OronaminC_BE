package com.oronaminc.join.websocket.api;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerCreateResponse;
import com.oronaminc.join.answer.dto.AnswerDeleteResponse;
import com.oronaminc.join.answer.dto.AnswerRequest;
import com.oronaminc.join.answer.dto.AnswerUpdateResponse;
import com.oronaminc.join.answer.mapper.AnswerMapper;
import com.oronaminc.join.answer.service.AnswerService;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.global.ratelimit.RateLimitService;
import com.oronaminc.join.global.ratelimit.RateLimitType;
import com.oronaminc.join.websocket.common.EventType;
import com.oronaminc.join.websocket.stomp.StompPrincipalUtil;

import io.github.bucket4j.Bucket;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AnswerWebsocketController {

    private final AnswerService answerService;
    private final RateLimitService rateLimitService;

    @MessageMapping("/rooms/{roomId}/question/{questionId}/answers/create")
    @SendTo("/topic/rooms/{roomId}/answers")
    public AnswerCreateResponse create(
        @DestinationVariable Long roomId,
        @DestinationVariable Long questionId,
        @Payload @Valid AnswerRequest request,
        Principal principal
    ) {
        Long memberId = StompPrincipalUtil.getMemberId(principal);

        Bucket bucket = rateLimitService.getBucket(RateLimitType.CREATE_ANSWER, roomId, memberId, questionId);

        if (!bucket.tryConsume(1)) {
            throw new ErrorException(TOO_MANY_REQUESTS_ANSWER);
        }

        Answer answer = answerService.create(roomId, memberId, questionId, request);

        log.info("답변 메세지 = {}", answer.getContent());

        return AnswerMapper.toAnswerCreateResponse(answer);
    }

    @MessageMapping("/rooms/{roomId}/answers/{answerId}/update")
    @SendTo("/topic/rooms/{roomId}/answers")
    public AnswerUpdateResponse update(
        @DestinationVariable Long roomId,
        @DestinationVariable Long answerId,
        @Payload @Valid AnswerRequest request,
        Principal principal
    ) {

        Long memberId = StompPrincipalUtil.getMemberId(principal);

        Answer answer = answerService.update(roomId, answerId, memberId, request);

        log.info("수정 메세지 = {}", answer.getContent());

        return AnswerMapper.toAnswerUpdateResponse(answer);
    }

    @MessageMapping("/rooms/{roomId}/answers/{answerId}/delete")
    @SendTo("/topic/rooms/{roomId}/answers")
    public AnswerDeleteResponse delete(
        @DestinationVariable Long roomId,
        @DestinationVariable Long answerId,
        Principal principal
    ) {
        Long memberId = StompPrincipalUtil.getMemberId(principal);

        answerService.delete(roomId, answerId, memberId);

        log.info("삭제되었습니다.");

        return new AnswerDeleteResponse(answerId, EventType.DELETE);
    }

}
