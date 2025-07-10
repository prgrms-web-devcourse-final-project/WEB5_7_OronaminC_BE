package com.oronaminc.join.answer.dto;

import com.oronaminc.join.global.dto.WriterDto;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record AnswerCreateResponse(
    //TODO: QuestionCreateResponse와 유사-> 둘중 하나만?
    String event,
    Long answerId,
    String content,
    int emojiCount,
    boolean isEojied,
    WriterDto writer,
    LocalDateTime createdAt
) {

}
