package com.oronaminc.join.question.util;


import com.oronaminc.join.global.dto.WriterDto;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.dto.QuestionCreateRequest;
import com.oronaminc.join.question.dto.QuestionCreateResponse;
import com.oronaminc.join.room.domain.Room;
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
}
