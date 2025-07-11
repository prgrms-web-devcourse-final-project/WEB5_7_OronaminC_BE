package com.oronaminc.join.emoji.dto;

import com.oronaminc.join.emoji.domain.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "발표방/질문/답변 공감 생성/삭제 요청 DTO")
public record EmojiRequest(
    @Schema(description = "공감 대상 타입 (ROOM, QUESTION, ANSWER)", example = "ROOM")
    TargetType targetType,
    @Schema(description = "공감 대상 ID", example = "1")
    Long targetId
) {

}
