package com.oronaminc.join.question.dto;


import com.oronaminc.join.global.dto.WriterDto;
import java.time.LocalDateTime;

public record QuestionResponseDto(
    String event,
    Long questionId,
    String content,
    int emojiCount,
    boolean hasAnswer,
    boolean isEmojied,
    WriterDto writer,
    LocalDateTime createdAt

) {

}
