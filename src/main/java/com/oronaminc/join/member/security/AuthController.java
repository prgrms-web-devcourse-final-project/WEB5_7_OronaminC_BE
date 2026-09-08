package com.oronaminc.join.member.security;


import com.oronaminc.join.member.dto.GuestLoginRequest;
import com.oronaminc.join.member.dto.KakaoLoginRequest;
import com.oronaminc.join.member.token.AuthTokenResponse;
import com.oronaminc.join.member.token.JwtTokenProvider;
import com.oronaminc.join.member.token.JwtUtils;
import com.oronaminc.join.member.token.LoginResponse;
import com.oronaminc.join.member.token.RefreshTokenStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "로그인 관련 API")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    @Operation(
        summary = "카카오 로그인"
    )
    @PostMapping("/kakao")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, AuthTokenResponse> kakaoLogin(
        @RequestBody KakaoLoginRequest kakaoLoginRequest,
        HttpServletResponse response
    ) {
        LoginResponse loginResponse = authService.kakaoLogin(kakaoLoginRequest.code());

        String refreshToken = loginResponse.refreshToken();

        JwtUtils.addRefreshTokenCookie(response, refreshToken,
            loginResponse.refreshTokenExpiresIn());

        return Map.of("token", loginResponse.authTokenResponse());
    }

    @Operation(
        summary = "비회원 로그인",
        responses = {
            @ApiResponse(responseCode = "201", description = "비회원 로그인 성공"),
            @ApiResponse(responseCode = "400", description = "닉네임 누락 또는 유효성 검증 실패")
        }
    )
    @PostMapping("/guest")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, AuthTokenResponse> guestLogin(
        @RequestBody @Valid GuestLoginRequest guestLoginRequest,
        HttpServletResponse response) {
        LoginResponse loginResponse = authService.loadGuest(guestLoginRequest);

        String refreshToken = loginResponse.refreshToken();

        JwtUtils.addRefreshTokenCookie(response, refreshToken,
            loginResponse.refreshTokenExpiresIn());

        return Map.of("token", loginResponse.authTokenResponse());
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

        String refresh = null;
        if(request.getCookies() != null){
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) refresh = cookie.getValue();
            }
        }

        if (refresh != null) {
            try {
                var body = jwtTokenProvider.parseClaims(refresh);
                refreshTokenStore.isBlacklisted(refresh);
                refreshTokenStore.saveLatest(body.memberId(), "");
            }catch (Exception ignored){ }
        }

        // 쿠키 제거
        ResponseCookie expired = ResponseCookie.from("refreshToken", "")
            .httpOnly(true).secure(true).sameSite("None")
                .path("/").maxAge(0).build();

        SecurityContextHolder.clearContext();

        /*HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();

        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);*/
    }
}
