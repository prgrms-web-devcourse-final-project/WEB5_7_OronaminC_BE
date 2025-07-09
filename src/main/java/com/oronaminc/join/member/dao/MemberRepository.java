package com.oronaminc.join.member.dao;

import com.oronaminc.join.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
