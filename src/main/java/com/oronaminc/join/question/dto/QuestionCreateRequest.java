package com.oronaminc.join.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "질문 생성 요청 DTO")
public record QuestionCreateRequest(
    @Schema(description = "질문 내용", example = "질문있습니다. 질문생성DTO가 맞나요?")
    String content
) {

}
