package com.oronaminc.join.member.dto;

import com.oronaminc.join.participant.domain.ParticipantType;

public enum ParticipationType {
    CREATED, JOINED;

    public static ParticipationType from(ParticipantType type) {
        return switch (type) {
            case PRESENTER -> CREATED;
            case TEAM, GUEST -> JOINED;
        };
    }
}
