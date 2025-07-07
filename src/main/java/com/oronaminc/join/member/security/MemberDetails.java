package com.oronaminc.join.member.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.oronaminc.join.member.domain.MemberType;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberDetails implements OAuth2User {
    private final Long id;
    private final String name;
    private final String nickname;
    private final Map<String, Object> attributes;
    private String role;

    @Builder
    public MemberDetails(Long id, String name, String nickname, Map<String, Object> attributes, MemberType role) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.attributes = attributes;
        this.role = role.name();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

}
