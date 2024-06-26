package com.jindo.minipay.member.repository;

import com.jindo.minipay.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
