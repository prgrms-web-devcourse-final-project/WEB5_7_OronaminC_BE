package com.oronaminc.join.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "발표방 생성 요청 DTO")
public record CreateRoomRequest(
        @NotBlank
        @Length(min = 3, max = 20)
        @Schema(description = "발표방 제목", example = "발표방1")
        String title,

        @NotBlank
        @Length(max = 150)
        @Schema(description = "발표방 간단한 설명", example = "오로나민C 발표방 입니다.")
        String description,

        @FutureOrPresent
        @Schema(description = "발표방 종료 날짜", example = "2025-07-16")
        LocalDate endDate,

        @Max(50)
        @Schema(description = "발표방 참여 제한 인원수", example = "16")
        Integer participantLimit,

        @NotBlank
        @Schema(description = "발표방 업로드 자료", example = "url")
        String documentUrl,

        @NotNull
        @Size(max = 5)
        @Schema(description = "발표방 추가한 팀원 목록", example = "{팀원1@example.com, 팀원2@example.com}")
        List<String> teamEmail
) {
}
