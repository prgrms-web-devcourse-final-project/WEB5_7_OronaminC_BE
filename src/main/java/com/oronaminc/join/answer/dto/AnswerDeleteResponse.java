package com.oronaminc.join.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "답변 삭제 응답 DTO")
public record AnswerDeleteResponse(
    Long answerId,
    @Schema(description = "삭제 이벤트", example = "DELETE")
    String event
) {

}
