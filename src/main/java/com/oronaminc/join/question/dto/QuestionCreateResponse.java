package com.oronaminc.join.question.dto;


import com.oronaminc.join.global.dto.WriterDto;
import com.oronaminc.join.websocket.common.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
@Schema(description = "질문 생성 응답 DTO")
public record QuestionCreateResponse(
    @Schema(description = "", example = "CREATE")
    EventType event,
    @Schema(description = "질문 ID", example = "11")
    Long questionId,
    @Schema(description = "질문 내용", example = "질문있습니다. 질문생성DTO가 맞나요?")
    String content,
    @Schema(description = "공감 수", example = "3")
    Long emojiCount,
    @Schema(description = "질문에 대한 답변 여부", example = "false")
    boolean hasAnswer,
    @Schema(description = "공감 여부", example = "true")
    boolean isEmojied,
    @Schema(description = "작성자 정보", example = "{ memberId: 21, nickname: 작성자1")
    WriterDto writer,
    @Schema(description = "질문 작성 시간", example = "2025-07-07T11:11:11")
    LocalDateTime createdAt

) {

}
