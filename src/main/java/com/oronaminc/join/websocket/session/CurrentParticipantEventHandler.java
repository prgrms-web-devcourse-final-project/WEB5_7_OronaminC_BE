package com.oronaminc.join.websocket.session;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.security.Principal;
import java.util.Set;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.room.event.RoomExitEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrentParticipantEventHandler {
    private final CurrentParticipantManager currentParticipantManager;

    private static final String ROOM_PREFIX = "/topic/rooms/";
    private static final String JOIN_SUFFIX = "/join";

    // @EventListener
    // public void handleSubscribe(SessionSubscribeEvent event) {
    //     StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    //     String destination = accessor.getDestination();
    //     Principal principal = accessor.getUser();
    //
    //     if (destination == null) {
    //         throw new ErrorException(STOMP_INVALID_DESTINATION);
    //     }
    //
    //     if (!destination.startsWith(ROOM_PREFIX)) {
    //         return;
    //     }
    //
    //     Long memberId = parseMemberId(principal);
    //     Long roomId = parseRoomId(destination);
    //
    //     if (!isRoomJoinPath(destination)) {
    //         validateParticipantRoomJoin(roomId, memberId);
    //     }
    // }

    @EventListener
    public void handleUnsubscribe(RoomExitEvent event) {
        currentParticipantManager.removeParticipant(event.memberId(), event.roomId());
    }

    private boolean isRoomJoinPath(String destination) {
        return destination.startsWith(ROOM_PREFIX) && destination.endsWith(JOIN_SUFFIX);
    }

    private void validateParticipantRoomJoin(Long roomId, Long memberId) {
        Set<Long> participants = currentParticipantManager.getRoomParticipants(roomId);
        if (participants == null || !participants.contains(memberId)) {
            throw new ErrorException(UNAUTHORIZED_NOT_JOIN_ROOM);
        }
    }

    private Long parseRoomId(String destination) {
        try {
            String[] parts = destination.split("/");
            return Long.valueOf(parts[3]);
        } catch (Exception e) {
            throw new ErrorException(STOMP_INVALID_DESTINATION);
        }
    }

    private Long parseMemberId(Principal principal) {
        try {
            return Long.valueOf(principal.getName());
        } catch (Exception e) {
            throw new ErrorException(SOCKET_BAD_REQUEST_MEMBER);
        }
    }
}
