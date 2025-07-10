package com.oronaminc.join.question.dto;

import java.util.List;

public record QuestionListResponse(
    List<QuestionAssembleResponse> questions
) {

}
