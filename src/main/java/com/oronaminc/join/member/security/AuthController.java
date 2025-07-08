package com.oronaminc.join.member.security;

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
import com.oronaminc.join.member.dto.SessionInfoResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/guest")
    @ResponseStatus(HttpStatus.CREATED)
    public GuestLoginResponse guestLogin(@RequestBody GuestLoginRequest guestLoginRequest, HttpServletRequest request) {
        MemberDetails guest = authService.loadGuest(guestLoginRequest);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                guest, null, List.of(new SimpleGrantedAuthority(guest.getRole()))
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return new GuestLoginResponse(guest.getId());
    }


    @GetMapping("/session")
    @ResponseStatus(HttpStatus.OK)
    public SessionInfoResponse getSessionInfo(@AuthenticationPrincipal MemberDetails memberDetails) {

        return new SessionInfoResponse(
                memberDetails.getId(),
                memberDetails.getName(),
                memberDetails.getNickname(),
                memberDetails.getRole()
        );
    }

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
