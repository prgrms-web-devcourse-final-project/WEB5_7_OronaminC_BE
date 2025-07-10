package com.oronaminc.join.participant.dao;

import com.oronaminc.join.participant.domain.Participant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query(
        "SELECT COUNT(p) > 0 " +
        "FROM Participant p " +
        "WHERE p.room.id = :roomId AND p.member.id = :memberId"
    )
    boolean existsByRoomIdAndMemberId(@Param("roomId") Long roomId, @Param("memberId") Long memberId);

    @Query(
        "SELECT p FROM Participant p "
            + "WHERE p.room.id = :roomId AND p.member.id = :memberId"
    )
    Optional<Participant> findByRoomIdAndMemberId( @Param("roomId") Long roomId, @Param("memberId") Long memberId );
}