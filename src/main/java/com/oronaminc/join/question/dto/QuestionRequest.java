package com.oronaminc.join.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "질문 생성/수정 요청 DTO")
public record QuestionRequest(
    @Schema(description = "질문 내용", example = "질문있습니다. 질문생성DTO가 맞나요?")
    @NotBlank(message = "질문 내용을 입력해주시기 바랍니다.")
    @Size(max = 500, message = "질문 내용은 최대 500자까지 입력할 수 있습니다.")
    String content,
    Long memberId
) {

}
