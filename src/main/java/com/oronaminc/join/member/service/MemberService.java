package com.oronaminc.join.member.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import org.springframework.stereotype.Service;

import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public boolean existsMemberByEmail(String email) {
        return memberRepository.existsMemberByEmail(email);
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ErrorException(NOT_FOUND_MEMBER));
    }
}
