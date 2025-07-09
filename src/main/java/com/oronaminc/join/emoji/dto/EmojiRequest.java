package com.oronaminc.join.emoji.dto;

import com.oronaminc.join.emoji.domain.TargetType;

public record EmojiRequest(
    TargetType targetType,
    Long targetId
) {

}
