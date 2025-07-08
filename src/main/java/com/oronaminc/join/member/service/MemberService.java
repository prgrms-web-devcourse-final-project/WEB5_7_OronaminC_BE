package com.oronaminc.join.member.service;

import org.springframework.stereotype.Service;

import com.oronaminc.join.member.repository.MemberJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberJpaRepository memberJpaRepository;

}
