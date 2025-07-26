package com.oronaminc.join.member.security;

import static com.oronaminc.join.member.util.MemberMapper.toSessionInfoResponse;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.oronaminc.join.member.dto.GuestLoginRequest;
import com.oronaminc.join.member.dto.GuestLoginResponse;
import com.oronaminc.join.member.dto.KakaoLoginRequest;
import com.oronaminc.join.member.dto.KakaoLoginResponse;
import com.oronaminc.join.member.dto.SessionInfoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "로그인 관련 API")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "카카오 로그인",
            description = "redirect url 에 포함된 파라미터의 code와 state를 입력해주세요. 이후 모든 요청에 세션 인증이 적용됩니다."
    )
    @PostMapping("/kakao")
    @ResponseStatus(HttpStatus.OK)
    public SessionInfoResponse kakaoLogin(
            @RequestBody KakaoLoginRequest kakaoLoginRequest,
            HttpServletRequest request
    ) {
        MemberDetails memberDetails = authService.kakaoLogin(kakaoLoginRequest.code());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                memberDetails, null, List.of(new SimpleGrantedAuthority(memberDetails.getRole()))
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return toSessionInfoResponse(memberDetails);
    }

    @Operation(
        summary = "비회원 로그인",
        description = "닉네임을 입력하면 비회원 세션이 생성되고 인증이 설정됩니다. 이후 모든 요청에 세션 인증이 적용됩니다.",
        responses = {
            @ApiResponse(responseCode = "201", description = "비회원 로그인 성공"),
            @ApiResponse(responseCode = "400", description = "닉네임 누락 또는 유효성 검증 실패")
        }
    )
    @PostMapping("/guest")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionInfoResponse guestLogin(@RequestBody @Valid GuestLoginRequest guestLoginRequest, HttpServletRequest request) {
        MemberDetails guest = authService.loadGuest(guestLoginRequest);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                guest, null, List.of(new SimpleGrantedAuthority(guest.getRole()))
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return toSessionInfoResponse(guest);
    }

    @Operation(
        summary = "현재 세션 사용자 정보 조회",
        description = "로그인한 사용자의 세션 정보를 반환합니다. 로그인하지 않은 경우 403 또는 401이 발생합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "세션 사용자 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "인증된 사용자 아님")
        }
    )
    @GetMapping("/session")
    @ResponseStatus(HttpStatus.OK)
    public SessionInfoResponse getSessionInfo(@AuthenticationPrincipal MemberDetails memberDetails) {

        return toSessionInfoResponse(memberDetails);
    }

    @Operation(
        summary = "로그아웃",
        description = "현재 로그인한 사용자의 세션을 만료시키고 인증 정보를 삭제합니다. JSESSIONID 쿠키도 제거됩니다.",
        responses = {
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "로그인되지 않은 사용자")
        }
    )
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();

        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
