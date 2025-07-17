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

    public EmojiResponse createEmoji(Long memberId, EmojiRequest emojiRequest) {
        for (int i = 0; i < 10; i++) {
            try {
                return emojiService.createEmoji(memberId, emojiRequest);
            } catch (ObjectOptimisticLockingFailureException e) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    throw new ErrorException(ErrorCode.CONFLICT_EMOJI);
                }
            }
        }
        throw new ErrorException(ErrorCode.CONFLICT_EMOJI);
    }

    public EmojiResponse deleteEmoji(Long memberId, EmojiRequest emojiRequest) {
        for (int i = 0; i < 10; i++) {
            try {
                return emojiService.deleteEmoji(memberId, emojiRequest);
            } catch (ObjectOptimisticLockingFailureException e) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    throw new ErrorException(ErrorCode.CONFLICT_EMOJI);
                }
            }
        }
        throw new ErrorException(ErrorCode.CONFLICT_EMOJI);
    }

}
