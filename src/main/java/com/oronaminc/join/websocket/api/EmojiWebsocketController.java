package com.oronaminc.join.websocket.api;

import com.oronaminc.join.emoji.dto.EmojiRequest;
import com.oronaminc.join.emoji.dto.EmojiResponse;
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

    @MessageMapping("/rooms/{roomId}/emojis/create")
    @SendTo("/topic/rooms/{roomId}/emojis")
    public EmojiResponse createEmoji(
        @DestinationVariable Long roomId,
        @Payload EmojiRequest emojiRequest,
        Principal principal
    ) {

        MemberDetails memberDetails = (MemberDetails) ((Authentication) principal).getPrincipal();
        Long memberId = memberDetails.getId();


        return null;
    }

}
