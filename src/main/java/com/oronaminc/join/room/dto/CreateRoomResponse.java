package com.oronaminc.join.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "발표방 생성 응답 DTO")
public record CreateRoomResponse(
        @Schema(description = "생성된 방 ID", example = "101")
        Long roomId,
        @Schema(description = "생성된 방 비밀코드", example = "12AB09")
        String secretCode
) {
}
