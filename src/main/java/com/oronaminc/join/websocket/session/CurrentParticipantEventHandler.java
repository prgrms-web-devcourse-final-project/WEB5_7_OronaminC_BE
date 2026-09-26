package com.oronaminc.join.websocket.session;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.oronaminc.join.room.event.RoomExitEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrentParticipantEventHandler {
    private final CurrentParticipantManager currentParticipantManager;

    @EventListener
    public void handleUnsubscribe(RoomExitEvent event) {
        currentParticipantManager.removeParticipant(event.memberId(), event.roomId());
    }
}
