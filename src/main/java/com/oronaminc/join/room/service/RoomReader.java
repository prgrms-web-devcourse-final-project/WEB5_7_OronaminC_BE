package com.oronaminc.join.room.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoomReader {
    private final RoomRepository roomRepository;

    public Optional<Room> findById(Long roomId) {
        return roomRepository.findById(roomId);
    }

    public Room getById(Long roomId) {
        return findById(roomId)
                .orElseThrow(() -> new ErrorException(NOT_FOUND_ROOM));
    }

    @Cacheable(cacheNames = "roomById")
    public Room getCacheById(Long roomId) {
        return findById(roomId)
                .orElseThrow(() -> new ErrorException(NOT_FOUND_ROOM));
    }

    public Optional<Room> findBySecretCode(String secretCode) {
        return roomRepository.findBySecretCode(secretCode);
    }

    public Room getBySecretCode(String secretCode) {
        return this.findBySecretCode(secretCode)
                .orElseThrow(() -> new ErrorException(NOT_FOUND_ROOM));
    }

    @Cacheable(cacheNames = "roomBySecretCode")
    public Room getCacheBySecretCode(String secretCode) {
        return this.findBySecretCode(secretCode)
                .orElseThrow(() -> new ErrorException(NOT_FOUND_ROOM));
    }

    public Boolean existsBySecretCode(String secretCode) {
        return roomRepository.existsBySecretCode(secretCode);
    }
}
