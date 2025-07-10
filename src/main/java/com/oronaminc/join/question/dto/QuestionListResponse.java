package com.oronaminc.join.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "질문 목록을 묶기 위한 DTO")
public record QuestionListResponse(
    List<QuestionAssembleResponse> questions
) {

}
