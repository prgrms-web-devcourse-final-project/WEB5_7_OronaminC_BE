package com.oronaminc.join.participant.dao;

import com.oronaminc.join.member.dto.ParticipantCountDto;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query("""
        select new com.oronaminc.join.member.dto.ParticipantCountDto(p.participantType, count(p))
        from Participant p
        where p.member.id = :memberId
        group by p.participantType
        """)
    List<ParticipantCountDto> countByMemberIdGroupByParticipantType(Long memberId);

    @Query("""
        select p
        from Participant p
        join fetch p.room
        where p.member.id = :memberId
        """)
    Page<Participant> findByMemberId(Long memberId, Pageable pageable);

    @Query("""
        select p
        from Participant p
        join fetch p.room
        where p.member.id = :memberId and p.participantType = :pType
        """)
    Page<Participant> findByMemberIdAndParticipantType(Long memberId, ParticipantType pType,
        Pageable pageable);

    @Query("""
        select p
        from Participant p
        join fetch p.room
        where p.member.id = :memberId and p.participantType != :pType
        """)
    Page<Participant> findByMemberIdAndParticipantTypeNot(Long memberId, ParticipantType pType,
        Pageable pageable);

}
