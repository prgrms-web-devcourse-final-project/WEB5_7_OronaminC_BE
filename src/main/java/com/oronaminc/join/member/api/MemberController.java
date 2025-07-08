package com.oronaminc.join.member.api;

import org.springframework.web.bind.annotation.RestController;

import com.oronaminc.join.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
}
