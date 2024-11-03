package com.jindo.minipay.member.repository;

import com.jindo.minipay.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);

    List<Member> findByIdIn(List<Long> ids);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByAccountNumber(String accountNumber);
}
