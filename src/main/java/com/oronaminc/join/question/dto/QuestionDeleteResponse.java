package com.oronaminc.join.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "질문 삭제 응답 DTO")
public record QuestionDeleteResponse(
    String event,
    Long questionId
) {
}
