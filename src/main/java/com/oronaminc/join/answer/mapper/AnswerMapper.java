package com.oronaminc.join.answer.mapper;

import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerCreateRequest;
import com.oronaminc.join.answer.dto.AnswerCreateResponse;
import com.oronaminc.join.global.dto.WriterDto;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.question.domain.Question;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnswerMapper {

    public static Answer toAnswer(Question question, Member member, AnswerCreateRequest request) {
        return Answer.create(question, member, request);
    }

    public static AnswerCreateResponse toAnswerCreateResponse(Answer answer) {
        return AnswerCreateResponse.builder()
            .event("CREATE")
            .answerId(answer.getId())
            .content(answer.getContent())
            .emojiCount(0)
            .isEojied(false)
            .writer(new WriterDto(
                answer.getMember().getId(),
                answer.getMember().getNickname()
            ))
            .createdAt(answer.getCreatedAt())
            .build();
    }
}
