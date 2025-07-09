package com.oronaminc.join.room.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oronaminc.join.room.domain.Room;

public interface RoomJpaRepository extends JpaRepository<Room,Long> {
    Boolean existsBySecretCode(String secretCode);
}
