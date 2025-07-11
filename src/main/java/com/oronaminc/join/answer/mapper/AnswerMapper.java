package com.oronaminc.join.answer.mapper;

import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerCreateResponse;
import com.oronaminc.join.global.dto.WriterDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnswerMapper {


    public static AnswerCreateResponse toAnswerCreateResponse(Answer answer) {
        return AnswerCreateResponse.builder()
            .event("CREATE")
            .answerId(answer.getId())
            .content(answer.getContent())
            .emojiCount(0)
            .isEmojied(false)
            .writer(new WriterDto(
                answer.getMember().getId(),
                answer.getMember().getNickname()
            ))
            .createdAt(answer.getCreatedAt())
            .build();
    }
}
