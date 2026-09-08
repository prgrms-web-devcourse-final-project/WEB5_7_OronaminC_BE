package com.oronaminc.join.member.util;

import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.member.dto.GuestLoginRequest;
import com.oronaminc.join.member.dto.KakaoUserResponse;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberMapper {

    public static Member toGuestMember(GuestLoginRequest guestLoginRequest) {
        return Member.builder()
            .email(null)
            .nickname(guestLoginRequest.nickname())
            .profileImage(null)
            .memberType(MemberType.GUEST)
            .build();
    }

    public static Member toNewKakaoMember(KakaoUserResponse kakaoUser) {
        return Member.builder()
            .email(kakaoUser.email())
            .nickname(kakaoUser.nickname())
            .profileImage(kakaoUser.profileImageUrl())
            .memberType(MemberType.MEMBER)
            .build();
    }

    public static KakaoUserResponse toKakaoUserResponse(Map<String, Object> kakaoAccount,
        Map<String, Object> profile) {
        return KakaoUserResponse.builder()
            .email((String) kakaoAccount.get("email"))
            .nickname((String) profile.get("nickname"))
            .profileImageUrl((String) profile.get("profile_image_url"))
            .build();
    }

}
