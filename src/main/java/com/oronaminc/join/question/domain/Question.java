package com.oronaminc.join.question.domain;

import com.oronaminc.join.global.entity.BaseEntity;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.question.dto.QuestionCreateRequest;
import com.oronaminc.join.room.domain.Room;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String content;

    private Long emojiCount;

    @Version
    private Integer version;

    public static Question create(Room room, Member member, QuestionCreateRequest requestDto) {
        return Question.builder()
            .room(room)
            .member(member)
            .content(requestDto.content())
            .emojiCount(0L)
            .build();
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
