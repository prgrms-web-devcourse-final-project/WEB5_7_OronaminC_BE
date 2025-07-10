package com.oronaminc.join.global.dev;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.domain.MemberType;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
public class DevController {
    private final MemberRepository memberRepository;

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.OK)
    public Member devJoin(@RequestBody DevJoinRequest devJoinRequest) {
        return memberRepository.save(
                Member.builder()
                .email(devJoinRequest.email())
                .nickname(devJoinRequest.nickname())
                .memberType(MemberType.MEMBER)
                .build()
        );
    }
}
