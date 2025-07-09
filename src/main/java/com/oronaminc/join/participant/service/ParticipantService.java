package com.oronaminc.join.participant.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.member.service.MemberService;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.participant.util.ParticipantMapper;
import com.oronaminc.join.room.domain.Room;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ParticipantService {
    private final ParticipantRepository participantRepository;
    private final MemberService memberService;

    public void savePresenterAndTeam(String presenterEmail, List<String> teamEmail, Room room) {
        saveParticipant(presenterEmail, room, ParticipantType.PRESENTER);
        for (String email : teamEmail) {
            saveParticipant(email, room, ParticipantType.TEAM);
        }
    }

    public void saveParticipant(String email, Room room, ParticipantType participantType) {
        Member participantMember = memberService.findByEmail(email);
        if (participantMember.getMemberType().equals(MemberType.GUEST)) {
            throw new ErrorException(UNAUTHORIZED_TEAM_GUEST);
        }
        Participant participant = ParticipantMapper.toParticipant(participantMember, room, participantType);
        participantRepository.save(participant);
    }

}
