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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void registerGuest() {
        this.email = "GUEST_" + this.id;
    }
}
