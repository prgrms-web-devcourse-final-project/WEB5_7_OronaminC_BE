package com.oronaminc.join.room.dto;

import java.util.List;

public record TopQnAResponse(
        String question,
        Long emojiCount,
        List<String> answers
) {
}
