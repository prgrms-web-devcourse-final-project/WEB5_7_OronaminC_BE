package com.oronaminc.join.question.mapper;


import com.oronaminc.join.global.dto.WriterDto;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.dto.QuestionRequestDto;
import com.oronaminc.join.question.dto.QuestionResponseDto;
import com.oronaminc.join.room.domain.Room;

public class QuestionMapper {

    public static Question toQuestion(Room room, Member member, QuestionRequestDto requestDto) {
        return Question.create(room, member, requestDto);
    }

    public static QuestionResponseDto toQuestionResponseDto (Question question) {
        return new QuestionResponseDto(
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
