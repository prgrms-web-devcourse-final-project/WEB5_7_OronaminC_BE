package com.oronaminc.join.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "답변 목록을 묶기 위한 DTO")
public record AnswerListResponse(
    List<AnswerGetResponse> answers
) {

}
