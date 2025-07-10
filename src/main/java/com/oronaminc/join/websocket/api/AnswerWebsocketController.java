package com.oronaminc.join.websocket.api;

import static com.oronaminc.join.global.exception.ErrorCode.NOT_FOUND_PARTICIPANT;
import static com.oronaminc.join.global.exception.ErrorCode.UNAUTHORIZED_ROLE_ANSWER;

import com.oronaminc.join.answer.dto.AnswerCreateRequest;
import com.oronaminc.join.answer.dto.AnswerCreateResponse;
import com.oronaminc.join.answer.service.AnswerService;
import com.oronaminc.join.answer.util.PermissionValidator;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.security.MemberDetails;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
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
        @Payload AnswerCreateRequest request,
        Principal principal
    ){
        MemberDetails memberDetails = (MemberDetails) ((Authentication) principal).getPrincipal();
        Long memberId = memberDetails.getId();

        permissionValidator.validateAnswerPermission(roomId, memberId);

        answerService.create();

        return null;
    }

}
