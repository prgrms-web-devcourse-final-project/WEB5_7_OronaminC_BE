package com.oronaminc.join.member.service;

import static com.oronaminc.join.global.exception.ErrorCode.NOT_FOUND_MEMBER;

import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
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

    public Member findById(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new ErrorException(NOT_FOUND_MEMBER));
    }
}
