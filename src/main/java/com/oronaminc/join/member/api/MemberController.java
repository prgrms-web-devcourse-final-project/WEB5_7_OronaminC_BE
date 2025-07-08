package com.oronaminc.join.member.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.oronaminc.join.member.dto.ExistsMemberRequest;
import com.oronaminc.join.member.dto.ExistsMemberResponse;
import com.oronaminc.join.member.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/exists")
    @ResponseStatus(HttpStatus.OK)
    public ExistsMemberResponse existsMemberByEmail(@Valid ExistsMemberRequest existsMemberRequest) {
        boolean exists = memberService.existsMemberByEmail(existsMemberRequest.email());
        return new ExistsMemberResponse(exists);
    }
}
