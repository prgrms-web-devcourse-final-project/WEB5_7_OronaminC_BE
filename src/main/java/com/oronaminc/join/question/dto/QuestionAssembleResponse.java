package com.oronaminc.join.question.dto;


import com.oronaminc.join.global.dto.WriterDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
@Schema(description = "프론트로 내려줄 질문 목록 응답 DTO")
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
