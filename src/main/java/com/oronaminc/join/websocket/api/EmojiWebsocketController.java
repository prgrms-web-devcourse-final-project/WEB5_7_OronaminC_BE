package com.oronaminc.join.websocket.api;

import com.oronaminc.join.emoji.dto.EmojiRequest;
import com.oronaminc.join.emoji.dto.EmojiResponse;
import com.oronaminc.join.emoji.service.EmojiFacade;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class EmojiWebsocketController {

    private final EmojiFacade emojiFacade;

    @MessageMapping("/rooms/{roomId}/emojis/create")
    @SendTo("/topic/rooms/{roomId}/emojis")
    public EmojiResponse createEmoji(
        @DestinationVariable Long roomId,
        @Payload EmojiRequest emojiRequest,
        Principal principal
    ) {
        Long memberId = Long.valueOf(principal.getName());

        return emojiFacade.createEmoji(memberId, emojiRequest);
    }

    @MessageMapping("/rooms/{roomId}/emojis/delete")
    @SendTo("/topic/rooms/{roomId}/emojis")
    public EmojiResponse deleteEmoji(
        @DestinationVariable Long roomId,
        @Payload EmojiRequest emojiRequest,
        Principal principal
    ) {
        Long memberId = Long.valueOf(principal.getName());

        return emojiFacade.deleteEmoji(memberId, emojiRequest);
    }

}
