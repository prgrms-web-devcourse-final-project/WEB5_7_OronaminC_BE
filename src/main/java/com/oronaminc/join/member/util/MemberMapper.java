package com.oronaminc.join.member.util;

import java.util.Map;

import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.member.dto.GuestLoginRequest;
import com.oronaminc.join.member.security.MemberDetails;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberMapper {
    public static MemberDetails toOAuth2MemberDetails(Member member) {
        return MemberDetails.builder()
                .id(member.getId())
                .name(member.getEmail())
                .nickname(member.getNickname())
                .role(member.getMemberType())
                .build();
    }

    public static MemberDetails toGuestMemberDetails(Member guest) {
        return MemberDetails.builder()
                .id(guest.getId())
                .name(guest.getEmail())
                .nickname(guest.getNickname())
                .role(MemberType.GUEST)
                .build();
    }

    public static Member toGuestMember(GuestLoginRequest guestLoginRequest) {
        return Member.builder()
                .email(null)
                .nickname(guestLoginRequest.nickname())
                .profileImage(null)
                .memberType(MemberType.GUEST)
                .build();
    }

    public static Member toKakaoMember(Map<String, Object> kakaoAccount, Map<String, Object> profile) {
        return Member.builder()
                .email(kakaoAccount.get("email").toString())
                .nickname(profile.get("nickname").toString())
                .profileImage(profile.get("profile_image_url").toString())
                .memberType(MemberType.MEMBER)
                .build();
    }
}
