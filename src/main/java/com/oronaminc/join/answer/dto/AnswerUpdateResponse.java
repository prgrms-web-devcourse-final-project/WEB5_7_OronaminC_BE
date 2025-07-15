package com.oronaminc.join.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "답변 수정 응답 DTO")
public record AnswerUpdateResponse(
    Long answerId,
    @Schema(description = "수정 이벤트", example = "UPDATE")
    String event,
    @Schema(description = "수정된 내용", example = "수정된 답변입니다.")
    String content

) {

}
