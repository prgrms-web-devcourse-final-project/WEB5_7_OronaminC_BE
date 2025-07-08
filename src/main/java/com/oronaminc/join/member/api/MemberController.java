package com.oronaminc.join.member.api;

import com.oronaminc.join.member.dto.MyPageType;
import com.oronaminc.join.member.dto.MyProfileGetResponse;
import com.oronaminc.join.member.dto.MyProfileUpdateRequest;
import com.oronaminc.join.member.dto.MyProfileUpdateResponse;
import com.oronaminc.join.member.dto.MyRoomsGetResponse;
import com.oronaminc.join.member.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PatchMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public MyProfileUpdateResponse updateMyProfile(MyProfileUpdateRequest request) {
        // todo : 세션로그인 도입 후 memberID 세션에서 받아오는 걸로 변경
        Long memberId = 1L;
        return myPageService.updateMyProfile(request, memberId);
    }

    @GetMapping("/rooms")
    @ResponseStatus(HttpStatus.OK)
    public MyRoomsGetResponse getMyProfile(@RequestParam(defaultValue = "ALL") MyPageType type,
        Pageable pageable) {
        // todo : 세션로그인 도입 후 memberID 세션에서 받아오는 걸로 변경
        Long memberId = 1L;
        return myPageService.getMyRooms(memberId, type, pageable);
    }

}
