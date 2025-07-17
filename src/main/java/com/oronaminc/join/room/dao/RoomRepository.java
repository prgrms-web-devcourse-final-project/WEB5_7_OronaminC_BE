package com.oronaminc.join.room.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oronaminc.join.room.domain.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Boolean existsBySecretCode(String secretCode);
    Optional<Room> findBySecretCode(String secretCode);
}
