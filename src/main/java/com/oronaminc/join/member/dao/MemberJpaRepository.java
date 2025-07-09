package com.oronaminc.join.member.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oronaminc.join.member.domain.Member;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
}
