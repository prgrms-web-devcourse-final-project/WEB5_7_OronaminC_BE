package com.oronaminc.join.websocket.api;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.room.dto.RoomJoinResponse;
import com.oronaminc.join.room.service.RoomService;
import com.oronaminc.join.websocket.config.ParticipantManager;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RoomWebsocketController {
    private final RoomService roomService;
    private final ParticipantService participantService;
    private final ParticipantManager participantManager;

    @MessageMapping("/rooms/{roomId}/join")
    @SendTo("/topic/rooms/{roomId}/join")
    public RoomJoinResponse joinRoom(
            @DestinationVariable Long roomId,
            Principal principal
    ) {
        Long memberId = Long.valueOf(principal.getName());
        participantService.validateParticipant(memberId, roomId);

        Integer limit = roomService.getRoomParticipantLimit(roomId);
        participantManager.addParticipant(roomId, memberId, limit);

        return new RoomJoinResponse(participantManager.getRoomParticipants(roomId).size());
    }
}
