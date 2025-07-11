package com.oronaminc.join.member.api;

import com.oronaminc.join.member.dto.ExistsMemberRequest;
import com.oronaminc.join.member.dto.ExistsMemberResponse;
import com.oronaminc.join.member.dto.MyPageType;
import com.oronaminc.join.member.dto.MyProfileGetResponse;
import com.oronaminc.join.member.dto.MyProfileUpdateRequest;
import com.oronaminc.join.member.dto.MyRoomsGetResponse;
import com.oronaminc.join.member.security.MemberDetails;
import com.oronaminc.join.member.service.MemberService;
import com.oronaminc.join.member.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관련 API")
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
    public ExistsMemberResponse existsMemberByEmail(
        @Valid ExistsMemberRequest existsMemberRequest) {
        boolean exists = memberService.existsMemberByEmail(existsMemberRequest.email());
        return new ExistsMemberResponse(exists);
    }

    @Operation(
        summary = "회원 프로필 조회",
        description = "회원의 닉네임, 생성한 방 수, 참여한 방 수를 조회합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 회원이 존재하지 않을 때 실패")
        }
    )
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public MyProfileGetResponse getMyProfile(@AuthenticationPrincipal MemberDetails memberDetails) {
        Long memberId = memberDetails.getId();
        return myPageService.getMyProfile(memberId);
    }

    @Operation(
        summary = "회원 프로필 수정",
        description = "닉네임을 입력받아 수정합니다.",
        responses = {
            @ApiResponse(responseCode = "204", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "404", description = "해당 회원이 존재하지 않을 때 실패")
        }
    )
    @PatchMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMyProfile(
        @RequestBody @Valid MyProfileUpdateRequest request,
        @AuthenticationPrincipal MemberDetails memberDetails) {
        Long memberId = memberDetails.getId();
        myPageService.updateMyProfile(request, memberId);
    }

    @Operation(
        summary = "회원이 생성하거나 참여한 발표방 목록 조회",
        description = """
            - type 파라미터를 통해 조회 범위를 지정할 수 있습니다.
              - ALL: 생성/참여한 방 모두 조회
              - CREATED: 회원이 생성한 방만 조회
              - JOINED: 회원이 참여한 방만 조회
            - page: 0부터 시작하는 페이지 번호
            - size: 한 페이지에 조회할 데이터 개수
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "발표방 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터(type 등)"),
            @ApiResponse(responseCode = "404", description = "해당 회원이 존재하지 않을 때 실패")
        }
    )
    @GetMapping("/rooms")
    @ResponseStatus(HttpStatus.OK)
    public MyRoomsGetResponse getMyProfile(
        @RequestParam(defaultValue = "ALL") MyPageType type,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();
        Pageable pageable = PageRequest.of(page, size);
        return myPageService.getMyRooms(memberId, type, pageable);
    }

}
