package com.oronaminc.join.member.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    private final MyPageService myPageService;

    @Operation(
        summary = "회원 존재 여부 확인",
        description = "입력한 이메일 주소를 가진 회원이 이미 존재하는지 확인합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "회원 존재 여부 확인 성공"),
            @ApiResponse(responseCode = "400", description = "이메일 형식 오류 또는 유효성 검증 실패")
        }
    )
    @GetMapping("/exists")
    @ResponseStatus(HttpStatus.OK)
    public ExistsMemberResponse existsMemberByEmail(@Valid ExistsMemberRequest existsMemberRequest) {
        boolean exists = memberService.existsMemberByEmail(existsMemberRequest.email());
        return new ExistsMemberResponse(exists);
    }

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
