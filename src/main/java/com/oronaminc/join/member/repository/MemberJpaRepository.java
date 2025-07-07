package com.oronaminc.join.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oronaminc.join.member.domain.Member;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
}
