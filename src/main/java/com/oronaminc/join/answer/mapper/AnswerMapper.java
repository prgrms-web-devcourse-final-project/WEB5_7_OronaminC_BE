package com.oronaminc.join.answer.mapper;

import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerCreateRequest;
import com.oronaminc.join.answer.dto.AnswerCreateResponse;
import com.oronaminc.join.answer.dto.AnswerGetResponse;
import com.oronaminc.join.global.dto.WriterDto;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.question.domain.Question;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnswerMapper {

    public static AnswerCreateResponse toAnswerCreateResponse(Answer answer) {
        return AnswerCreateResponse.builder()
            .questionId(answer.getQuestion().getId())
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

    public static AnswerGetResponse toAnswerGetResponse(Answer answer, Long emojiCount, boolean isEmojied) {
        return AnswerGetResponse.builder()
            .answerId(answer.getId())
            .emojiCount(emojiCount)
            .Emojied(isEmojied)
            .content(answer.getContent())
            .writer(new WriterDto(
                answer.getMember().getId(),
                answer.getMember().getNickname()
            ))
            .createdAt(answer.getCreatedAt())
            .build();
    }

    public static Answer toEntity(Question question, Member member, AnswerCreateRequest request) {
        return Answer.create(question, member, request.content());
    }
}
