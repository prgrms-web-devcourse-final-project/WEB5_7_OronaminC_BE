package com.oronaminc.join.room.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record ReportResponse(
        Long roomId,
        String title,
        Long totalView,
        Long totalQuestions,
        Double answerRate,
        Long totalEmojis,
        List<TopQnAResponse> topQnA
) {
}
