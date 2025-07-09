package com.oronaminc.join.member.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import org.springframework.stereotype.Service;

import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.dao.MemberJpaRepository;
import com.oronaminc.join.member.domain.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberJpaRepository memberJpaRepository;

    public boolean existsMemberByEmail(String email) {
        return memberJpaRepository.existsMemberByEmail(email);
    }

    public Member findByEmail(String email) {
        return memberJpaRepository.findByEmail(email)
                .orElseThrow(() -> new ErrorException(NOT_FOUND_MEMBER));
    }
}
