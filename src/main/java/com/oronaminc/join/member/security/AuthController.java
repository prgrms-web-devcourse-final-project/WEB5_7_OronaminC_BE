package com.oronaminc.join.member.security;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.dto.GuestLoginRequest;
import com.oronaminc.join.member.dto.SessionInfoResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/guest")
    @ResponseStatus(HttpStatus.OK)
    public Long guestLogin(@RequestBody GuestLoginRequest guestLoginRequest) {
        MemberDetails guest = authService.loadGuest(guestLoginRequest);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                guest, null, List.of(new SimpleGrantedAuthority(guest.getRole()))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return guest.getId();
    }

    @GetMapping("/session")
    public SessionInfoResponse getSessionInfo(@AuthenticationPrincipal MemberDetails memberDetails) {
        if (memberDetails == null) {
            throw new ErrorException(UNAUTHORIZED_MEMBER);
        }

        return new SessionInfoResponse(
                memberDetails.getId(),
                memberDetails.getName(),
                memberDetails.getNickname(),
                memberDetails.getRole()
        );
    }
}
