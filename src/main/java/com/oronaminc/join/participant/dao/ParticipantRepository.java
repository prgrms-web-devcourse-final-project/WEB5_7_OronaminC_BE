package com.oronaminc.join.participant.dao;

import java.util.List;
import java.util.Optional;

import com.oronaminc.join.member.dto.ParticipantCountDto;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query("""
        select COUNT(p) > 0 
        from Participant p 
        where p.room.id = :roomId and p.member.id = :memberId
    """)
    boolean existsByRoomIdAndMemberId(@Param("roomId") Long roomId, @Param("memberId") Long memberId);

    Optional<Participant> findByRoomIdAndParticipantType(Long roomId, ParticipantType participantType);
    List<Participant> findAllByRoomIdAndParticipantType(Long roomId, ParticipantType participantType);

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

    Optional<Participant> findByRoomIdAndMemberId( @Param("roomId") Long roomId, @Param("memberId") Long memberId );

    @Query("""
        select case when COUNT(p) > 0 then true else false end 
        from Participant p
        where p.room.id = :roomId
        and p.member.id = :memberId
        and (p.participantType = com.oronaminc.join.participant.domain.ParticipantType.PRESENTER
            or p.participantType = com.oronaminc.join.participant.domain.ParticipantType.TEAM)
    """)
    boolean existsPresenterOrTeamByMemberId(@Param("roomId") Long roomId, @Param("memberId") Long memberId);

    void deleteByRoomId(Long roomId);

    @Query(value = """
        select COUNT(*) 
        from participant
        where room_id = :roomId
        and exited_at is not null
        and TIMESTAMPDIFF(SECOND, created_at, exited_at) >= 30
    """, nativeQuery = true)
    Long countParticipantsStayedOver30Seconds(@Param("roomId") Long roomId);
}