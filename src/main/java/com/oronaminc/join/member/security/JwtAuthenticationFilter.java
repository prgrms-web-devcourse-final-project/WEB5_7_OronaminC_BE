package com.oronaminc.join.member.security;

import com.oronaminc.join.member.token.JwtTokenProvider;
import com.oronaminc.join.member.token.TokenBody;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                TokenBody body = jwtTokenProvider.parseClaims(token);
                MemberDetails memberDetails = MemberDetails.builder()
                    .id(body.memberId())
                    .name(body.nickname())
                    .nickname(body.nickname())
                    .attributes(Map.of())
                    .role(body.role())
                    .build();
                var auth = new UsernamePasswordAuthenticationToken(
                    memberDetails,
                    null,
                    memberDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }catch (Exception e){

            }
        }

        filterChain.doFilter(request,response);
    }
}
