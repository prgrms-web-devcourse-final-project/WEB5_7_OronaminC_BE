package com.oronaminc.join.participant.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oronaminc.join.participant.domain.Participant;

public interface ParticipantJpaRepository extends JpaRepository<Participant, Long> {
}
