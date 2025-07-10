package com.oronaminc.join.question.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
@Schema(description = "정렬 쿼리 결과를 위한 DTO")
public record QuestionFlatResponse(
    Long questionId,
    String content,
    Long emojiCount,
    boolean hasAnswer,
    boolean isEmojied,
    Long memberId,
    String nickname,
    LocalDateTime createdAt

) {

}
