package com.oronaminc.join.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "답변 생성/수정 요청 DTO")
public record AnswerRequest(
    //TODO: 빈값 or " " (space) 처리
    @NotBlank(message = "답변 내용을 입력해주시기 바랍니다.")
    @Size(max = 300, message = "답변 내용은 최대 300자까지 입력할 수 있습니다.")
    @Schema(description = "답변 내용", example = "답변입니다.")
    String content
) {

}
