package com.oronaminc.join.question.dto;


import java.time.LocalDateTime;
import lombok.Builder;

@Builder
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
