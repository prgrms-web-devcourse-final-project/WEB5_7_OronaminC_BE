package com.oronaminc.join.websocket.api;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.oronaminc.join.room.dto.RoomJoinResponse;
import com.oronaminc.join.room.service.RoomService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RoomWebsocketController {
    private final RoomService roomService;

    @MessageMapping("/rooms/{roomId}/join")
    @SendTo("/topic/rooms/{roomId}/join")
    public RoomJoinResponse joinRoom(
            @DestinationVariable Long roomId,
            Principal principal
    ) {
        Long memberId = Long.valueOf(principal.getName());
        return roomService.subscribeRoom(roomId, memberId);
    }
}
