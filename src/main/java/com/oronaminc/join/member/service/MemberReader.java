package com.oronaminc.join.member.service;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberReader {
    private final MemberRepository memberRepository;

    public Optional<Member> findById(Long memberId) {
        return memberRepository.findById(memberId);
    }

    public Member getById(Long memberId) {
        return this.findById(memberId)
                .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_MEMBER));
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public Member getByEmail(String email) {
        return this.findByEmail(email)
                .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_MEMBER));
    }

    public boolean existsByEmail(String email) {
        return memberRepository.existsMemberByEmail(email);
    }

}
