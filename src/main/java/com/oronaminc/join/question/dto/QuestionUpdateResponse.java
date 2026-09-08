package com.oronaminc.join.question.dto;

import com.oronaminc.join.websocket.common.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "질문 수정 응답 DTO")
public record QuestionUpdateResponse(
    EventType event,
    Long questionId,
    String content

    ) {
}
