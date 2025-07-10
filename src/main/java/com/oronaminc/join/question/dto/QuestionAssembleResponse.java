package com.oronaminc.join.question.dto;


import com.oronaminc.join.global.dto.WriterDto;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record QuestionAssembleResponse(
    Long questionId,
    String content,
    Long emojiCount,
    boolean hasAnswer,
    boolean isEmojied,
    WriterDto writer,
    LocalDateTime createdAt

) {

}
