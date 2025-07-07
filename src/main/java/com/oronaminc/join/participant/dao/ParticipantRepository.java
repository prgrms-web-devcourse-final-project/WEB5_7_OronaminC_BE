package com.oronaminc.join.participant.dao;

import com.oronaminc.join.member.dto.ParticipantCountDto;
import com.oronaminc.join.participant.domain.Participant;
import java.util.List;
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

}
