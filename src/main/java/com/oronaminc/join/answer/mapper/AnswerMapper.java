package com.oronaminc.join.answer.mapper;

import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerCreateResponse;
import com.oronaminc.join.answer.dto.AnswerGetResponse;
import com.oronaminc.join.answer.dto.AnswerListResponse;
import com.oronaminc.join.answer.dto.AnswerRequest;
import com.oronaminc.join.answer.dto.AnswerUpdateResponse;
import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.emoji.service.EmojiReader;
import com.oronaminc.join.global.dto.WriterDto;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.question.domain.Question;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

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

    public static AnswerGetResponse toAnswerGetResponse(Answer answer, boolean isEmojied) {

        return AnswerGetResponse.builder()
            .answerId(answer.getId())
            .emojiCount(answer.getEmojiCount())
            .isEmojied(isEmojied)
            .content(answer.getContent())
            .writer(new WriterDto(
                answer.getMember().getId(),
                answer.getMember().getNickname()
            ))
            .createdAt(answer.getCreatedAt())
            .build();
    }

    public static Answer toEntity(Question question, Member member, AnswerRequest request) {
        return Answer.create(question, member, request.content());
    }

    public static AnswerUpdateResponse toAnswerUpdateResponse(Answer answer) {
        return AnswerUpdateResponse.builder()
            .answerId(answer.getId())
            .event("UPDATE")
            .content(answer.getContent())
            .build();
    }

    public static AnswerListResponse toAnswerListResponse(
        Slice<AnswerGetResponse> slice) {
        return new AnswerListResponse(slice.getContent());
    }

}
