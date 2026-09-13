package com.oronaminc.join.question.domain;

import com.oronaminc.join.global.entity.BaseEntity;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.question.dto.QuestionRequest;
import com.oronaminc.join.room.domain.Room;
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
@Table(name = "question", indexes = {
    @Index(name = "idx_question_id_room", columnList = "room_id")
})
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String content;

    private Long emojiCount;

    @Version
    private Integer version;

    public static Question create(Room room, Member member, QuestionRequest requestDto) {
        return Question.builder()
            .room(room)
            .member(member)
            .content(requestDto.content())
            .emojiCount(0L)
            .build();
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public Long incrementEmojiCount() {
        return ++this.emojiCount;
    }

    public Long decrementEmojiCount() {
        if (this.emojiCount > 0) {
            this.emojiCount--;
        }
        return this.emojiCount;
    }

}
