package com.oronaminc.join.emoji.service;

import com.oronaminc.join.emoji.dto.EmojiRequest;
import com.oronaminc.join.emoji.dto.EmojiResponse;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmojiFacade {

    private final EmojiService emojiService;

    public EmojiResponse toggleEmoji(Long memberId, EmojiRequest emojiRequest) {
        for (int i = 0; i < 10; i++) {
            try {
                return emojiService.toggleEmoji(memberId, emojiRequest);
            } catch (ObjectOptimisticLockingFailureException e) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    throw new ErrorException(ErrorCode.EMOJI_CONFLICT);
                }
            }
        }
        throw new ErrorException(ErrorCode.EMOJI_CONFLICT);
    }

}
