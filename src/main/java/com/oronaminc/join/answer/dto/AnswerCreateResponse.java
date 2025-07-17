package com.oronaminc.join.answer.dto;

import com.oronaminc.join.global.dto.WriterDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
@Schema(description = "WebSocket STOMP 통신 답변 응답 DTO")
public record AnswerCreateResponse(
    //TODO: QuestionCreateResponse와 유사-> 둘중 하나만?
    @Schema(description = "답변이 생성될 질문 ID")
    Long questionId,
    @Schema(description = "답변 생성/삭제/수정 상태", example = "CREATE")
    String event,
    @Schema(description = "답변 ID", example = "11")
    Long answerId,
    @Schema(description = "답변 내용", example = "답변입니다.")
    @NotBlank(message = "답변 내용을 입력해주시기 바랍니다.")
    @Size(max = 300, message = "답변 내용은 최대 300자까지 입력할 수 있습니다.")
    String content,
    @Schema(description = "답변 내용에 대한 공감 수", example = "23")
    int emojiCount,
    @Schema(description = "답변 공감 여부", example = "true")
    boolean isEmojied,
    @Schema(description = "작성자 정보  DTO")
    WriterDto writer,
    LocalDateTime createdAt
) {

}
