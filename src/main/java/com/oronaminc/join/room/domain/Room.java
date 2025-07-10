package com.oronaminc.join.room.domain;

import com.oronaminc.join.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
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
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    private String secretCode;

    @Enumerated(EnumType.STRING)
    private RoomStatus roomStatus;

    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    private Long emojiCount;

    private Integer participantLimit;

    private LocalDateTime endedAt;

    @Version
    private Integer version;

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
