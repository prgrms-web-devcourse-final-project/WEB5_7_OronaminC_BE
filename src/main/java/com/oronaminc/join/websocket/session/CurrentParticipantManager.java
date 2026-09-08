package com.oronaminc.join.websocket.session;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.oronaminc.join.global.exception.ErrorException;

@Component
public class CurrentParticipantManager {
    private final Map<Long, Set<Long>> roomParticipants = new ConcurrentHashMap<>();

    public Set<Long> getRoomParticipants(Long roomId) {
        createRoom(roomId);
        return roomParticipants.get(roomId);
    }

    public void createRoom(Long roomId) {
        roomParticipants.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet());
    }

    // public void addParticipant(Long roomId, Long memberId, int limit) {
    //     Set<Long> participants = getRoomParticipants(roomId);
    //
    //     if (participants.contains(memberId)) return;
    //
    //     synchronized (participants) {
    //         if (participants.size() >= limit) {
    //             throw new ErrorException(UNAUTHORIZED_LIMIT_PARTICIPANT);
    //         }
    //         participants.add(memberId);
    //     }
    // }

    public void addParticipant(Long roomId, Long memberId, int limit) {
        roomParticipants.compute(roomId, (id, participants) -> {
            participants = getRoomParticipants(roomId);

            // 중복 참가자일 경우 그대로 반환 (변화 없음)
            if (participants.contains(memberId)) {
                return participants;
            }

            // 인원 초과 시 예외 발생
            if (participants.size() >= limit) {
                throw new ErrorException(UNAUTHORIZED_LIMIT_PARTICIPANT);
            }

            participants.add(memberId);
            return participants;
        });
    }

    public void removeParticipant(Long memberId, Long roomId) {
        roomParticipants.get(roomId).remove(memberId);
    }
}
