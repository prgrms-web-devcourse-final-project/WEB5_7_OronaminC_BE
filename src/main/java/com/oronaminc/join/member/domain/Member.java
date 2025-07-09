package com.oronaminc.join.member.domain;

import com.oronaminc.join.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String nickname;
    private String profileImage;

    @Enumerated(EnumType.STRING)
    private MemberType memberType;

    @Builder
    public Member(String email, String nickname, String profileImage, MemberType memberType) {
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.memberType = memberType;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
