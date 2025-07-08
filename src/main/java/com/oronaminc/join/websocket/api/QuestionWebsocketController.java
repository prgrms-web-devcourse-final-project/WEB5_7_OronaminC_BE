package com.oronaminc.join.websocket.api;

import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.dto.QuestionCreateRequest;
import com.oronaminc.join.question.dto.QuestionCreateResponse;
import com.oronaminc.join.question.mapper.QuestionMapper;
import com.oronaminc.join.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class QuestionWebsocketController {

    private final QuestionService questionService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/rooms/{roomId}/questions/create")
    @SendTo("/topic/rooms/{roomId}/questions")
    public QuestionCreateResponse create(
        @DestinationVariable Long roomId,
        @Payload QuestionCreateRequest request
    ) {

        // 시큐리티 추가되면 수정하겠습니다
        Long memberId = 1L;

        Question question = questionService.create(roomId, memberId, request);

        log.info("수신한 메시지 = {}", request.content());

        return QuestionMapper.toQuestionCreateResponse(question);
    }


}
