package com.oronaminc.join.member.security;

import static com.oronaminc.join.member.util.MemberMapper.*;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.dto.GuestLoginRequest;
import com.oronaminc.join.member.dto.KakaoUserResponse;
import com.oronaminc.join.member.service.MemberReader;
import com.oronaminc.join.member.util.MemberMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;
    private final MemberReader memberReader;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    // @Override
    // public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    //     OAuth2User oAuth2User = super.loadUser(userRequest);
    //     Map<String, Object> attributes = oAuth2User.getAttributes();
    //
    //     log.info("attributes :: " + attributes);
    //
    //     Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
    //     Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
    //
    //
    //     Optional<Member> optionalMember = memberReader.findByEmail(kakaoAccount.get("email").toString());
    //
    //     Member member = optionalMember.orElseGet(() -> memberRepository.save(toKakaoMember(kakaoAccount, profile)));
    //
    //     return toOAuth2MemberDetails(member);
    // }

    @Transactional
    public MemberDetails loadGuest(GuestLoginRequest guestLoginRequest) {
        Member guest = toGuestMember(guestLoginRequest);

        memberRepository.save(guest);
        guest.registerGuest();

        return toGuestMemberDetails(guest);
    }

    @Transactional
    public MemberDetails kakaoLogin(String code) {
        String accessToken = getAccessToken(code);
        KakaoUserResponse kakaoUser = getUserInfo(accessToken);

        Member member = memberRepository.findByEmail(kakaoUser.email())
                .orElseGet(() -> memberRepository.save(MemberMapper.toNewKakaoMember(kakaoUser)));

        return toOAuth2MemberDetails(member);
    }

    private String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        params.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(TOKEN_URI, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    private KakaoUserResponse getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(USER_INFO_URI, HttpMethod.GET, entity, Map.class);

        Map<String, Object> attributes = response.getBody();

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return MemberMapper.toKakaoUserResponse(kakaoAccount, profile);
    }
}
