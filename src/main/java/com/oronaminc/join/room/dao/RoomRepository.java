package com.oronaminc.join.room.dao;

import com.oronaminc.join.room.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Integer> {

}
