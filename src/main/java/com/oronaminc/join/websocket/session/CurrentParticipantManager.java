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
            // compute 안에서 같은 맵을 다시 수정(getRoomParticipants -> computeIfAbsent)하면
            // ConcurrentHashMap이 IllegalStateException(Recursive update)을 던지므로 여기서 직접 생성한다
            if (participants == null) {
                participants = ConcurrentHashMap.newKeySet();
            }

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
