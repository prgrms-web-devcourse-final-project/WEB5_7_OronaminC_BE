package com.oronaminc.join.member.security;

import static com.oronaminc.join.member.util.MemberMapper.*;

import java.util.Map;
import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.dto.GuestLoginRequest;
import com.oronaminc.join.member.service.MemberReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;
    private final MemberReader memberReader;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.info("attributes :: " + attributes);

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");


        Optional<Member> optionalMember = memberReader.findByEmail(kakaoAccount.get("email").toString());

        Member member = optionalMember.orElseGet(() -> memberRepository.save(toKakaoMember(kakaoAccount, profile)));

        return toOAuth2MemberDetails(member);
    }

    @Transactional
    public MemberDetails loadGuest(GuestLoginRequest guestLoginRequest) {
        Member guest = toGuestMember(guestLoginRequest);

        memberRepository.save(guest);
        guest.registerGuest();

        return toGuestMemberDetails(guest);
    }

}
