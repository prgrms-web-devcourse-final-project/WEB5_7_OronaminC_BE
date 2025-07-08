package com.oronaminc.join.question.controller.websocket;

import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.dto.QuestionCreateRequest;
import com.oronaminc.join.question.dto.QuestionCreateResponse;
import com.oronaminc.join.question.mapper.QuestionMapper;
import com.oronaminc.join.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class QuestionWebsocketController {

    private final QuestionService questionService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/rooms/{roomId}/questions/create")
//    @SendTo("/topic/rooms/{roomId}/questions")
    public QuestionCreateResponse create(
        @DestinationVariable Long roomId,
        @Payload QuestionCreateRequest request
    ) {

        // 시큐리티 추가되면 수정하겠습니다
        Long memberId = 1L;

        Question question = questionService.create(roomId, memberId, request);

        System.out.println("메시지 수신: " + request.content());
        messagingTemplate.convertAndSend(
            "/topic/rooms/" + roomId + "/questions", request
        );

        return QuestionMapper.toQuestionCreateResponse(question);
    }


}
