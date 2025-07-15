package com.oronaminc.join.member.dto;

import com.oronaminc.join.participant.domain.ParticipantType;

public record ParticipantCountDto(
    ParticipantType participantType,
    Long count
) {

}
