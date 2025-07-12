package com.oronaminc.join.answer.dto;

import com.oronaminc.join.global.dto.WriterDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
@Schema(description = "답변 보기 클릭시 보여지는 답변 응답 DTO")
public record AnswerGetResponse(
    Long answerId,
    @Schema(description = "답변 내용에 대한 공감 수", example = "23")
    Long emojiCount,
    @Schema(description = "답변 공감 여부", example = "true")
    boolean Emojied,
    @Schema(description = "답변 내용", example = "답변입니다.")
    String content,
    @Schema(description = "작성자 정보  DTO")
    WriterDto writer,
    LocalDateTime createdAt
) {

}
