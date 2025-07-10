package com.oronaminc.join.question.util;


import com.oronaminc.join.global.dto.WriterDto;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.dto.QuestionCreateRequest;
import com.oronaminc.join.question.dto.QuestionCreateResponse;
import com.oronaminc.join.question.dto.QuestionFlatResponse;
import com.oronaminc.join.question.dto.QuestionListResponse;
import com.oronaminc.join.room.domain.Room;
import java.io.IOException;
import java.io.Writer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuestionMapper {

    public static Question toQuestion(Room room, Member member, QuestionCreateRequest request) {
        return Question.create(room, member, request);
    }

    public static QuestionCreateResponse toQuestionCreateResponse (Question question) {
        return QuestionCreateResponse.builder()
            .event("CREATE")
            .questionId(question.getId())
            .content(question.getContent())
            .emojiCount(0L)
            .isEmojied(false)
            .hasAnswer(false)
            .writer(new WriterDto(
                question.getMember().getId(),
                question.getMember().getNickname()
            ))
            .createdAt(question.getCreatedAt())
            .build();
    }

    public static QuestionListResponse toQuestionListResponse(QuestionFlatResponse flatResponse) {
        return QuestionListResponse.builder()
            .questionId(flatResponse.questionId())
            .content(flatResponse.content())
            .isEmojied(flatResponse.isEmojied())
            .hasAnswer(flatResponse.hasAnswer())
            .emojiCount(flatResponse.emojiCount())
            .writer(new WriterDto(
                flatResponse.memberId(),
                flatResponse.nickname()
            ))
            .createdAt(flatResponse.createdAt())
            .build();
    }
}
