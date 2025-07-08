package com.oronaminc.join.participant.repository;

import com.oronaminc.join.participant.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

}
