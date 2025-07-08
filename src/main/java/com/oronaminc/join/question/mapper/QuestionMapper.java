package com.oronaminc.join.question.mapper;


import com.oronaminc.join.global.dto.WriterDto;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.dto.QuestionCreateRequest;
import com.oronaminc.join.question.dto.QuestionCreateResponse;
import com.oronaminc.join.room.domain.Room;

public class QuestionMapper {

    public static Question toQuestion(Room room, Member member, QuestionCreateRequest request) {
        return Question.create(room, member, request);
    }

    public static QuestionCreateResponse toQuestionCreateResponse (Question question) {
        return new QuestionCreateResponse(
            "CREATE",
            question.getId(),
            question.getContent(),
            0,
            false,
            false,
            new WriterDto(
                question.getMember().getId(),
                question.getMember().getNickname()
            ),
            question.getCreatedAt()
        );
    }
}
