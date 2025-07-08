package com.oronaminc.join.member.security;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.oronaminc.join.member.dto.GuestLoginRequest;

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
}
