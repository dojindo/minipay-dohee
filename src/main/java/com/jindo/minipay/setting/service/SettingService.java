package com.jindo.minipay.setting.service;

import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.setting.entity.Setting;
import com.jindo.minipay.setting.exception.SettingException;
import com.jindo.minipay.setting.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_MEMBER;

@RequiredArgsConstructor
@Service
public class SettingService {
    private final MemberRepository memberRepository;
    private final SettingRepository settingRepository;

    public void createSetting(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new SettingException(NOT_FOUND_MEMBER));

        settingRepository.save(Setting.create(member));
    }
}
