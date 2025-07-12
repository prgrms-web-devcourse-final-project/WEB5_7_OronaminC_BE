package com.oronaminc.join.websocket.api;

import com.oronaminc.join.emoji.dto.EmojiRequest;
import com.oronaminc.join.emoji.dto.EmojiResponse;
import com.oronaminc.join.emoji.service.EmojiService;
import com.oronaminc.join.member.security.MemberDetails;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class EmojiWebsocketController {

    private final EmojiService emojiService;

    @MessageMapping("/rooms/{roomId}/emojis")
    @SendTo("/topic/rooms/{roomId}/emojis")
    public EmojiResponse toggleEmoji(
        @DestinationVariable Long roomId,
        @Payload EmojiRequest emojiRequest,
        Principal principal
    ) {
        Long memberId = Long.valueOf(principal.getName());

        return emojiService.toggleEmoji(memberId, emojiRequest);
    }

}
