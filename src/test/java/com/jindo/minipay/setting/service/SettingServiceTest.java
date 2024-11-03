package com.jindo.minipay.setting.service;

import com.jindo.minipay.global.exception.ErrorCode;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.setting.exception.SettingException;
import com.jindo.minipay.setting.repository.SettingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SettingServiceTest {
    @Mock
    MemberRepository memberRepository;

    @Mock
    SettingRepository settingRepository;

    @InjectMocks
    SettingService settingService;

    @Test
    @DisplayName("회원에 대한 설정 정보를 생성한다.")
    void createSetting() {
        // given
        Member sender = Member.builder()
                .email("test@test.com")
                .password("test12345")
                .name("tester1")
                .build();

        given(memberRepository.findById(1L))
                .willReturn(Optional.of(sender));

        // when
        settingService.createSetting(1L);

        // then
        verify(settingRepository).save(any());
    }

    @Test
    @DisplayName("회원을 찾을 수 없으면 예외가 발생한다.")
    void createSetting_not_found_member() {
        // given
        given(memberRepository.findById(1L))
                .willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> settingService.createSetting(1L))
                .isInstanceOf(SettingException.class)
                .hasMessage(ErrorCode.NOT_FOUND_MEMBER.getMessage());
    }
}