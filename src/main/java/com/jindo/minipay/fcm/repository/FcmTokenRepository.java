package com.jindo.minipay.fcm.repository;

import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.fcm.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByMember(Member member);
}
