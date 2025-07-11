package com.oronaminc.join.answer.domain;

import com.oronaminc.join.answer.dto.AnswerCreateRequest;
import com.oronaminc.join.global.entity.BaseEntity;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.question.domain.Question;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// TODO: ddl-auto: create,update에만 유효 -> 추후 flyway sql 생성
@Table(name = "answer", indexes = {
    @Index(name = "idx_answer_question_member", columnList = "question_id, member_id")
})
public class Answer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String content;
    private Long emojiCount;

    @Version
    private Integer version;

    public static Answer create(Question question, Member member, AnswerCreateRequest requestDto) {
        return Answer.builder()
            .question(question)
            .member(member)
            .content(requestDto.content())
            .emojiCount(0L)
            .build();
    }

}
