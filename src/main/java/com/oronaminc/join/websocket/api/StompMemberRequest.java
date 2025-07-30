package com.oronaminc.join.websocket.api;

import jakarta.validation.constraints.NotNull;

public record StompMemberRequest(
        @NotNull
        Long memberId
) {
}
