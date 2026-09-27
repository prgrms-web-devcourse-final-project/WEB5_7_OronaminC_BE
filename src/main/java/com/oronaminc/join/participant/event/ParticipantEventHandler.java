package com.oronaminc.join.participant.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.room.event.RoomExitEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ParticipantEventHandler {

    private final ParticipantService participantService;

    @EventListener
    public void exitRoomEvent(RoomExitEvent roomExitEvent) {
        participantService.updateExitAt(roomExitEvent.roomId(), roomExitEvent.memberId());
    }
}
