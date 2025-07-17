package com.oronaminc.join.websocket.api;

import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerCreateRequest;
import com.oronaminc.join.answer.dto.AnswerCreateResponse;
import com.oronaminc.join.answer.mapper.AnswerMapper;
import com.oronaminc.join.answer.service.AnswerService;
import com.oronaminc.join.answer.util.PermissionValidator;
import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.question.dao.QuestionRepository;
import com.oronaminc.join.room.dao.RoomRepository;
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
        @Valid @Payload AnswerCreateRequest request,
        Principal principal
    ) {
        Long memberId = Long.valueOf(principal.getName());

        permissionValidator.validateAnswerPermission(roomId, memberId);

        Answer answer = answerService.create(roomId, memberId, questionId, request);

        log.info("답변 메세지 = {}", request.content());

        return AnswerMapper.toAnswerCreateResponse(answer);
    }

}
