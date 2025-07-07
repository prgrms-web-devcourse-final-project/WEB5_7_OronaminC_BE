package com.oronaminc.join.member.security;

import java.util.Map;
import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.member.dto.GuestLoginRequest;
import com.oronaminc.join.member.repository.MemberJpaRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService extends DefaultOAuth2UserService {
    private final MemberJpaRepository memberJpaRepository;

    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.info("attributes :: " + attributes);

        httpSession.setAttribute("login_info", attributes);

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");


        Optional<Member> optionalMember = memberJpaRepository.findByEmail(kakaoAccount.get("email").toString());

        Member member = optionalMember.orElseGet(
                () -> memberJpaRepository.save(
                        Member.builder()
                                .email(kakaoAccount.get("email").toString())
                                .nickname(profile.get("nickname").toString())
                                .profileImage(profile.get("profile_image_url").toString())
                                .memberType(MemberType.MEMBER)
                                .build()
                )
        );

        MemberDetails memberDetails = MemberDetails.builder()
                .id(member.getId())
                .name(member.getEmail())
                .nickname(member.getNickname())
                .attributes(attributes)
                .role(member.getMemberType())
                .build();


        return memberDetails;
    }

    @Transactional
    public MemberDetails loadGuest(GuestLoginRequest guestLoginRequest) {
        Member guest = Member.builder()
                .email(null)
                .nickname(guestLoginRequest.nickname())
                .profileImage(null)
                .memberType(MemberType.GUEST)
                .build();

        memberJpaRepository.save(guest);

        // 1. 비회원 MemberDetails 생성
        MemberDetails memberDetails = MemberDetails.builder()
                .id(guest.getId()) // DB ID 없음
                .name("GUEST" + guest.getId())
                .nickname(guest.getNickname())
                .role(MemberType.GUEST) // enum: GUEST 추가
                .attributes(Map.of("nickname", guest.getNickname()))
                .build();

        return memberDetails;
    }

}
