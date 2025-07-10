package com.oronaminc.join.emoji.service;

import org.springframework.stereotype.Service;

import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.emoji.repository.EmojiRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmojiService {
    private final EmojiRepository emojiRepository;

    public Integer countRoomEmoji(Long roomId) {
        return emojiRepository.countByTargetIdAndTargetType(roomId, TargetType.ROOM);
    }
}
