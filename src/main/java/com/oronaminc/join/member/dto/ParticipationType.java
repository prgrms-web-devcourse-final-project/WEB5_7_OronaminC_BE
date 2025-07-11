package com.oronaminc.join.member.dto;

import com.oronaminc.join.participant.domain.ParticipantType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "발표방 참여 타입: CREATED(생성자), JOINED(참여자)")
public enum ParticipationType {
    CREATED, JOINED;

    public static ParticipationType from(ParticipantType type) {
        return switch (type) {
            case PRESENTER -> CREATED;
            case TEAM, GUEST -> JOINED;
        };
    }
}
