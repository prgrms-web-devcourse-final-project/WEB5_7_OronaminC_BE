package com.oronaminc.join.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberReader memberReader;

    public boolean existsMemberByEmail(String email) {
        return memberReader.existsByEmail(email);
    }
}
