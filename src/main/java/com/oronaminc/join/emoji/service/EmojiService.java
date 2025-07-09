package com.oronaminc.join.emoji.service;

import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.emoji.dao.EmojiRepository;
import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.emoji.dto.EmojiRequest;
import com.oronaminc.join.emoji.dto.EmojiResponse;
import com.oronaminc.join.question.dao.QuestionRepository;
import com.oronaminc.join.room.dao.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmojiService {

    private final EmojiRepository emojiRepository;
    private final RoomRepository roomRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    @Transactional
    public EmojiResponse createEmoji(Long memberId, EmojiRequest emojiRequest) {
        Long emojiCount = 0L;
        switch (emojiRequest.targetType()) {
            case ROOM -> {
//                roomRepository.findEmojiCountById()
            }
            case QUESTION -> {

            }
            case ANSWER -> {

            }
        }

    }


}
