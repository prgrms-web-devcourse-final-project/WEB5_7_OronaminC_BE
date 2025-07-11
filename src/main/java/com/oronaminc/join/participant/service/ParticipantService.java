package com.oronaminc.join.participant.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.member.service.MemberReader;
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
    private final MemberReader memberReader;
    private final ParticipantReader participantReader;

    public void savePresenterAndTeam(String presenterEmail, List<String> teamEmail, Room room) {
        saveMemberParticipantByEmail(presenterEmail, room, ParticipantType.PRESENTER);
        for (String email : teamEmail) {
            saveMemberParticipantByEmail(email, room, ParticipantType.TEAM);
        }
    }

    public void saveMemberParticipantByEmail(String email, Room room, ParticipantType participantType) {
        Member participantMember = memberReader.getByEmail(email);
        if (participantMember.getMemberType().equals(MemberType.GUEST)) {
            throw new ErrorException(UNAUTHORIZED_TEAM_GUEST);
        }
        Participant participant = ParticipantMapper.toParticipant(participantMember, room, participantType);
        participantRepository.save(participant);
    }
    
    public void saveParticipantById(Long memberId, Room room, ParticipantType participantType) {
        Member participantMember = memberReader.getById(memberId);
        if (participantReader.existsByRoomIdAndMemberId(room.getId(), participantMember.getId())) {
            return;
        }
        Participant participant = ParticipantMapper.toParticipant(participantMember, room, participantType);
        participantRepository.save(participant);
    }

    public void validateParticipant(Long memberId, Long roomId) {
        if (!participantReader.existsByRoomIdAndMemberId(roomId, memberId)) {
            throw new ErrorException(NOT_FOUND_PARTICIPANT);
        }
    }

    public Participant getPresenter(Long roomId) {
        return participantReader.findByRoomIdAndParticipantType(roomId, ParticipantType.PRESENTER)
                .orElseThrow(() -> new ErrorException(NOT_FOUND_PARTICIPANT));
    }

    public List<Participant> getTeam(Long roomId) {
        return participantReader.findAllByRoomIdAndParticipantType(roomId, ParticipantType.TEAM);
    }

    public void updateTeam(Room room, List<String> emails) {
        List<Participant> team = this.getTeam(room.getId());
        for (Participant participant : team) {
            if (!emails.contains(participant.getMember().getEmail())) {
                participantRepository.delete(participant);
            }
        }

        for (String email : emails) {
            this.saveMemberParticipantByEmail(email, room, ParticipantType.TEAM);
        }
    }

    public void validatePresenter(Long roomId, Long memberId) {
        Participant presenter = this.getPresenter(roomId);
        if (!presenter.getMember().getId().equals(memberId)) {
            throw new ErrorException(UNAUTHORIZED_UPDATE_AND_DELETE);
        }
    }

    public void deleteParticipantByRoomId(Long roomId) {
        participantRepository.deleteByRoomId(roomId);
    }
}
