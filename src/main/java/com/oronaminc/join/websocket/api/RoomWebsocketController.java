package com.oronaminc.join.websocket.api;

import java.security.Principal;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.oronaminc.join.room.dto.RoomJoinResponse;
import com.oronaminc.join.room.service.RoomService;
import com.oronaminc.join.websocket.session.WebsocketSessionManager;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RoomWebsocketController {
    private final RoomService roomService;
    private final WebsocketSessionManager sessionManager;

    @MessageMapping("/rooms/{roomId}/join")
    @SendTo("/topic/rooms/{roomId}/join")
    public RoomJoinResponse joinRoom(
            @DestinationVariable Long roomId,
            Principal principal,
            Message<?> message
    ) {
        Long memberId = Long.valueOf(principal.getName());
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String sessionId = accessor.getSessionId();

        // 세션 매니저에서 세션 정보 활용
        RoomJoinResponse response = roomService.subscribeRoom(roomId, memberId);
        sessionManager.addAttribute(sessionId, "roomId", roomId);

        return response;
    }
}
