package com.oronaminc.join.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "질문 수정 응답 DTO")
public record QuestionUpdateResponse(
    String event,
    Long questionId,
    String content

    ) {
}
