package com.oronaminc.join.websocket.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oronaminc.join.member.security.MemberDetails;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.dto.QuestionCreateRequest;
import com.oronaminc.join.question.dto.QuestionCreateResponse;
import com.oronaminc.join.question.util.QuestionMapper;
import com.oronaminc.join.question.service.QuestionService;
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
public class QuestionWebsocketController {

    private final QuestionService questionService;
    private final ObjectMapper objectMapper;

    @MessageMapping("/rooms/{roomId}/questions/create")
    @SendTo("/topic/rooms/{roomId}/questions")
    public QuestionCreateResponse create(
        @DestinationVariable Long roomId,
        @Payload QuestionCreateRequest request,
        Principal principal
    ) {

        MemberDetails memberDetails = (MemberDetails) ((Authentication) principal).getPrincipal();
        Long memberId = memberDetails.getId();

        Question question = questionService.create(roomId, memberId, request);

        log.info("수신한 메시지 = {}", request.content());

        return QuestionMapper.toQuestionCreateResponse(question);
    }


}
