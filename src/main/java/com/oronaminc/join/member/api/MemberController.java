package com.oronaminc.join.member.api;

import com.oronaminc.join.member.dto.MyProfileGetResponse;
import com.oronaminc.join.member.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MyPageService myPageService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public MyProfileGetResponse getMyProfile() {
        // todo : 세션로그인 도입 후 memberID 세션에서 받아오는 걸로 변경
        Long memberId = 1L;
        return myPageService.getMyProfile(memberId);
    }

}
