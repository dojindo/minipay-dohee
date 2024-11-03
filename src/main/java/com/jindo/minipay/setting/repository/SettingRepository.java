package com.jindo.minipay.setting.repository;

import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.setting.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findByMember(Member member);
}
