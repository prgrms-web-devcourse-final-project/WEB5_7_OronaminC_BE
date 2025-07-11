package com.oronaminc.join.participant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.dto.ParticipantCountDto;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ParticipantReader {
    private final ParticipantRepository participantRepository;

    public boolean existsByRoomIdAndMemberId(Long roomId, Long memberId) {
        return participantRepository.existsByRoomIdAndMemberId(roomId, memberId);
    }

    public Optional<Participant> findByRoomIdAndParticipantType(Long roomId, ParticipantType type) {
        return participantRepository.findByRoomIdAndParticipantType(roomId, type);
    }

    public List<Participant> findAllByRoomIdAndParticipantType(Long roomId, ParticipantType type) {
        return participantRepository.findAllByRoomIdAndParticipantType(roomId, type);
    }

    public List<ParticipantCountDto> countByMemberIdGroupByParticipantType(Long memberId) {
        return participantRepository.countByMemberIdGroupByParticipantType(memberId);
    }

    public Page<Participant> findByMemberId(Long memberId, Pageable pageable) {
        return participantRepository.findByMemberId(memberId, pageable);
    }

    public Page<Participant> findByMemberIdAndParticipantType(Long memberId, ParticipantType type, Pageable pageable) {
        return participantRepository.findByMemberIdAndParticipantType(memberId, type, pageable);
    }

    public Page<Participant> findByMemberIdAndParticipantTypeNot(Long memberId, ParticipantType type, Pageable pageable) {
        return participantRepository.findByMemberIdAndParticipantTypeNot(memberId, type, pageable);
    }

    public Optional<Participant> findByRoomIdAndMemberId(Long roomId, Long memberId) {
        return participantRepository.findByRoomIdAndMemberId(roomId, memberId);
    }

    public Participant getByRoomIdAndMemberId(Long roomId, Long memberId) {
        return this.findByRoomIdAndMemberId(roomId, memberId)
                .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_PARTICIPANT));
    }

    public void deleteByRoomId(Long roomId) {
        participantRepository.deleteByRoomId(roomId);
    }

}
