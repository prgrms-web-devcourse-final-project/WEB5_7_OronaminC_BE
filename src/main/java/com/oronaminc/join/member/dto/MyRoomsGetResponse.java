package com.oronaminc.join.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "마이페이지 발표방 목록 조회 응답 DTO")
public record MyRoomsGetResponse(
    @Schema(description = "발표방 목록", example = "[{...}, {...}]")
    List<MyRoomsDto> content,
    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    int currentPage,
    @Schema(description = "페이지 크기", example = "10")
    int size,
    @Schema(description = "전체 데이터 개수", example = "105")
    long totalElements,
    @Schema(description = "전체 페이지 수", example = "11")
    int totalPages
) {

}
