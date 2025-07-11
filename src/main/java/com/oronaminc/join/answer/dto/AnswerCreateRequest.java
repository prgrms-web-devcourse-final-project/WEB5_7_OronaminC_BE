package com.oronaminc.join.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "WebSocket STOMP 통신 답변 요청 DTO")
public record AnswerCreateRequest(
    //TODO: 빈값 or " " (space) 처리
    @Schema(description = "답변 내용", example = "답변입니다.")
    String content
) {

}
