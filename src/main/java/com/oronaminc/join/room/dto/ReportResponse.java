package com.oronaminc.join.room.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ReportResponse(
        Long roomId,
        String title,
        Long totalView,
        Long totalQuestions,
        Double answerRate,
        Long totalEmojis,
        List<TopQnADto> topQnA
) {
}
