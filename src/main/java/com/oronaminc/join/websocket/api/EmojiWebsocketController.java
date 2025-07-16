package com.oronaminc.join.websocket.api;

import com.oronaminc.join.emoji.dto.EmojiRequest;
import com.oronaminc.join.emoji.dto.EmojiResponse;
import com.oronaminc.join.emoji.service.EmojiFacade;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.global.ratelimit.RateLimitService;
import com.oronaminc.join.global.ratelimit.RateLimitType;
import io.github.bucket4j.Bucket;
import jakarta.validation.Valid;
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
    private final RateLimitService rateLimitService;

    @MessageMapping("/rooms/{roomId}/emojis/create")
    @SendTo("/topic/rooms/{roomId}/emojis")
    public EmojiResponse createEmoji(
        @DestinationVariable Long roomId,
        @Payload @Valid EmojiRequest emojiRequest,
        Principal principal
    ) {
        Long memberId = Long.valueOf(principal.getName());

        Bucket bucket = rateLimitService.getBucket(RateLimitType.EMOJI, memberId,
            emojiRequest.targetType(), emojiRequest.targetId());

        if (!bucket.tryConsume(1)) {
            throw new ErrorException(ErrorCode.TOO_MANY_REQUESTS_EMOJI);
        }

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

        Bucket bucket = rateLimitService.getBucket(RateLimitType.EMOJI, memberId,
            emojiRequest.targetType(), emojiRequest.targetId());
        if (!bucket.tryConsume(1)) {
            throw new ErrorException(ErrorCode.TOO_MANY_REQUESTS_EMOJI);
        }

        return emojiFacade.deleteEmoji(memberId, emojiRequest);
    }

}
