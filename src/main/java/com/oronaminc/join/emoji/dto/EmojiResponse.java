package com.oronaminc.join.emoji.dto;

import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.websocket.common.EventType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "발표방/질문/답변 공감 생성/삭제 응답 DTO")
public record EmojiResponse(
    @Schema(description = "이벤트 타입 (CREATE, DELETE)", example = "CREATE")
    EventType event,
    @Schema(description = "공감 대상 타입 (ROOM, QUESTION, ANSWER)", example = "ROOM")
    TargetType targetType,
    @Schema(description = "공감 대상 ID", example = "1")
    Long targetId,
    @Schema(description = "공감 총 개수", example = "15")
    Long emojiCount
) {

}
