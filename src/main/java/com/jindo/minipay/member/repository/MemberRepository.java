package com.jindo.minipay.member.repository;

import com.jindo.minipay.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);

    List<Member> findByIdIn(List<Long> ids);
}
