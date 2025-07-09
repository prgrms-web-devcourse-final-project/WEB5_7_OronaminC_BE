package com.oronaminc.join.room.domain;

import java.time.LocalDateTime;

import com.oronaminc.join.global.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
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

    @Builder
    public Room(String title, String description, String secretCode, RoomStatus roomStatus, RoomType roomType,
            Long emojiCount, Integer participantLimit, LocalDateTime endedAt) {
        this.title = title;
        this.description = description;
        this.secretCode = secretCode;
        this.roomStatus = roomStatus;
        this.roomType = roomType;
        this.emojiCount = emojiCount;
        this.participantLimit = participantLimit;
        this.endedAt = endedAt;
    }
}
