package com.oronaminc.join.answer.util;

import static com.oronaminc.join.global.exception.ErrorCode.NOT_FOUND_PARTICIPANT;
import static com.oronaminc.join.global.exception.ErrorCode.UNAUTHORIZED_ROLE_ANSWER;

import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionValidator {

    private final ParticipantRepository participantRepository;

    public void validateAnswerPermission(Long roomId, Long memberId) {
        Participant participant = participantRepository.findByRoomIdAndMemberId(roomId,memberId)
            .orElseThrow(() -> new ErrorException(NOT_FOUND_PARTICIPANT));
        ParticipantType type = participant.getParticipantType();

        if(type == ParticipantType.GUEST){
            throw new ErrorException(UNAUTHORIZED_ROLE_ANSWER);
        }
    }

}
