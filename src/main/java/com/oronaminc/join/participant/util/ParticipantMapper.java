package com.oronaminc.join.participant.util;

import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.room.domain.Room;

public class ParticipantMapper {
    public static Participant toParticipant(Member member,  Room room, ParticipantType participantType) {
        return Participant.builder()
                .room(room)
                .member(member)
                .participantType(participantType)
                .build();
    }
}
