package com.oronaminc.join.member.domain;

import com.oronaminc.join.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String nickname;

    @Enumerated(EnumType.STRING)
    private MemberType memberType;

    @Builder
    public Member(String email, String nickname, MemberType memberType) {
        this.email = email;
        this.nickname = nickname;
        this.memberType = memberType;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
