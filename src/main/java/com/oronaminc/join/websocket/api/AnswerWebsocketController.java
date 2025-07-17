package com.oronaminc.join.websocket.api;

import static com.oronaminc.join.global.exception.ErrorCode.UNAUTHORIZED_MEMBER;

import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerCreateResponse;
import com.oronaminc.join.answer.dto.AnswerDeleteResponse;
import com.oronaminc.join.answer.dto.AnswerRequest;
import com.oronaminc.join.answer.dto.AnswerUpdateResponse;
import com.oronaminc.join.answer.mapper.AnswerMapper;
import com.oronaminc.join.answer.service.AnswerService;
import com.oronaminc.join.answer.util.PermissionValidator;
import com.oronaminc.join.global.exception.ErrorException;
import jakarta.validation.Valid;
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
public class AnswerWebsocketController {

    private final AnswerService answerService;
    private final PermissionValidator permissionValidator;

    @MessageMapping("/rooms/{roomId}/question/{questionId}/answers/create")
    @SendTo("/topic/rooms/{roomId}/answers")
    public AnswerCreateResponse create(
        @DestinationVariable Long roomId,
        @DestinationVariable Long questionId,
        @Payload @Valid AnswerRequest request,
        Principal principal
    ) {
        Long memberId = getMemberId(principal);

        permissionValidator.validateAnswerPermission(roomId, memberId);

        Answer answer = answerService.create(roomId, memberId, questionId, request);

        log.info("답변 메세지 = {}", request.content());

        return AnswerMapper.toAnswerCreateResponse(answer);
    }

    @MessageMapping("/answers/{answerId}/update")
    @SendTo("/topic/rooms/{roomId}/answers")
    public AnswerUpdateResponse update(
        @DestinationVariable Long answerId,
        @Payload @Valid AnswerRequest request,
        Principal principal
    ) {

        Long memberId = getMemberId(principal);

        permissionValidator.validateAnswerUpdatePermission(answerId, memberId);

        Answer answer = answerService.update(answerId, request);

        return AnswerMapper.toAnswerUpdateResponse(answer);
    }

    @MessageMapping("/answers/{answerId}/delete")
    @SendTo("/topic/rooms/{roomId}/answers")
    public AnswerDeleteResponse delete(
        @DestinationVariable Long roomId,
        @DestinationVariable Long questionId,
        @DestinationVariable Long answerId,
        Principal principal
    ) {
        Long memberId = getMemberId(principal);

        permissionValidator.validateAnswerDeletePermission(answerId, memberId);

        answerService.delete(answerId);

        return new AnswerDeleteResponse(answerId, "DELETE");
    }

    private Long getMemberId(Principal principal) {
        if (principal == null) {
            throw new ErrorException(UNAUTHORIZED_MEMBER);
        }
        return Long.valueOf(principal.getName());
    }

}
