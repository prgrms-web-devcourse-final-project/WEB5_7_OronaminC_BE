package com.oronaminc.join.emoji.dto;

import com.oronaminc.join.emoji.domain.TargetType;

public record EmojiResponse(
    String event,
    TargetType targetType,
    Long targetId,
    Long emojiCount
) {

}
