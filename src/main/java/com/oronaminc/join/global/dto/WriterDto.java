package com.oronaminc.join.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "작성자 정보 DTO")
public record WriterDto(
    @Schema(description = "작성자 ID", example = "21")
    Long memberId,
    @Schema(description = "작성자 닉네임", example = "작성자1")
    String nickname
) {

}
