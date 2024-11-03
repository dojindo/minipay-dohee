package com.jindo.minipay.member.service;

import com.jindo.minipay.account.checking.event.dto.CreateCheckingAccountEvent;
import com.jindo.minipay.member.dto.RegisterRequest;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.exception.MemberException;
import com.jindo.minipay.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static com.jindo.minipay.global.exception.ErrorCode.ALREADY_EXISTS_MEMBER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    MemberRepository memberRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    MemberService memberService;

    @Nested
    @DisplayName("회원 등록 메서드")
    class RegisterMethod {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@test.com")
                .password("test12345")
                .name("tester1")
                .build();

        @Test
        @DisplayName("회원을 등록한다.")
        void register() {
            // given
            Member member = Member.builder()
                    .email("test@test.com")
                    .password("test12345")
                    .name("tester1")
                    .build();

            ReflectionTestUtils.setField(member, "id", 1L);

            CreateCheckingAccountEvent event =
                    new CreateCheckingAccountEvent(1L);

            given(memberRepository.existsByEmail(request.email()))
                    .willReturn(false);

            given(memberRepository.save(any()))
                    .willReturn(member);

            doNothing().when(eventPublisher).publishEvent(event);

            // when
            Long memberId = memberService.register(request);

            // then
            assertEquals(1L, memberId);
            verify(eventPublisher, times(1))
                    .publishEvent(event);
        }

        @Test
        @DisplayName("이미 존재하는 이메일이면 예외가 발생한다.")
        void register_existsEmail() {
            // given
            given(memberRepository.existsByEmail((request.email())))
                    .willReturn(true);

            // when
            // then
            assertThatThrownBy(() -> memberService.register(request))
                    .isInstanceOf(MemberException.class)
                    .hasMessageContaining(ALREADY_EXISTS_MEMBER.getMessage());
        }
    }
}