package com.oronaminc.join.websocket.config;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.oronaminc.join.global.exception.ErrorException;

@Component
public class ParticipantManager {
    private final Map<Long, Set<Long>> roomParticipants = new ConcurrentHashMap<>();

    public Set<Long> getRoomParticipants(Long roomId) {
        createRoom(roomId);
        return roomParticipants.get(roomId);
    }

    public void createRoom(Long roomId) {
        roomParticipants.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet());
    }

    public void addParticipant(Long roomId, Long memberId, int limit) {
        Set<Long> participants = getRoomParticipants(roomId);

        if (participants.contains(memberId)) return;

        synchronized (participants) {
            if (participants.size() >= limit) {
                throw new ErrorException(UNAUTHORIZED_LIMIT_PARTICIPANT);
            }
            participants.add(memberId);
        }
    }

    public void removeMember(Long memberId) {
        for (Set<Long> participants : roomParticipants.values()) {
            participants.remove(memberId);
        }
    }
}
