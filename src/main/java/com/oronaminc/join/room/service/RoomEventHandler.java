package com.oronaminc.join.room.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.oronaminc.join.document.service.DocumentService;
import com.oronaminc.join.emoji.service.EmojiService;
import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.question.service.QuestionService;
import com.oronaminc.join.room.dto.RoomDeleteEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoomEventHandler {
    private final ParticipantService participantService;
    private final QuestionService questionService;
    private final EmojiService emojiService;
    private final DocumentService documentService;

    @EventListener
    public void handleRoomDelete(RoomDeleteEvent event) {
        participantService.deleteParticipantByRoomId(event.roomId());
        questionService.deleteByRoomId(event.roomId());
        emojiService.deleteByRoomEmoji(event.roomId());
        documentService.deleteByRoomId(event.roomId());
    }
}
