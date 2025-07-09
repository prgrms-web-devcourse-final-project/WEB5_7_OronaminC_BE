package com.oronaminc.join.member.api;

import com.oronaminc.join.member.security.MemberDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.oronaminc.join.member.dto.ExistsMemberRequest;
import com.oronaminc.join.member.dto.ExistsMemberResponse;
import com.oronaminc.join.member.service.MemberService;

import jakarta.validation.Valid;
import com.oronaminc.join.member.dto.MyPageType;
import com.oronaminc.join.member.dto.MyProfileGetResponse;
import com.oronaminc.join.member.dto.MyProfileUpdateRequest;
import com.oronaminc.join.member.dto.MyProfileUpdateResponse;
import com.oronaminc.join.member.dto.MyRoomsGetResponse;
import com.oronaminc.join.member.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

    private final MyPageService myPageService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public MyProfileGetResponse getMyProfile(@AuthenticationPrincipal MemberDetails memberDetails) {
        Long memberId = memberDetails.getId();
        return myPageService.getMyProfile(memberId);
    }

    @PatchMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public MyProfileUpdateResponse updateMyProfile(
        @RequestBody @Valid MyProfileUpdateRequest request,
        @AuthenticationPrincipal MemberDetails memberDetails) {
        // todo : 세션로그인 도입 후 memberID 세션에서 받아오는 걸로 변경
        Long memberId = memberDetails.getId();
        return myPageService.updateMyProfile(request, memberId);
    }

    @GetMapping("/rooms")
    @ResponseStatus(HttpStatus.OK)
    public MyRoomsGetResponse getMyProfile(
        @RequestParam(defaultValue = "ALL") MyPageType type,
        @AuthenticationPrincipal MemberDetails memberDetails,
        Pageable pageable) {
        Long memberId = memberDetails.getId();
        return myPageService.getMyRooms(memberId, type, pageable);
    }

}
