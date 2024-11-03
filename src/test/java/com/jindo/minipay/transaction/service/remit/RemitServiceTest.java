package com.jindo.minipay.transaction.service.remit;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.setting.entity.Setting;
import com.jindo.minipay.setting.repository.SettingRepository;
import com.jindo.minipay.transaction.dto.RemitRequest;
import com.jindo.minipay.transaction.exception.TransactionException;
import com.jindo.minipay.transaction.repository.TransactionRepository;
import com.jindo.minipay.transaction.service.remit.strategy.impl.ImmediatelyRemitStrategy;
import com.jindo.minipay.transaction.service.remit.strategy.impl.PendingRemitStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_SETTING;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RemitServiceTest {
    @Mock
    MemberRepository memberRepository;

    @Mock
    SettingRepository settingRepository;

    @Mock
    ImmediatelyRemitStrategy immediatelyRemitStrategy;

    @Mock
    PendingRemitStrategy pendingRemitStrategy;

    @Mock
    TransactionRepository transactionRepository;

    @InjectMocks
    RemitService remitService;

    @Nested
    @DisplayName("송금하기 메서드")
    class RemitMethod {
        String senderAccountNumber = "8888-01-1234567";
        String receiverAccountNumber = "8888-02-7654321";

        Member sender = Member.builder()
                .email("test@test.com")
                .password("test12345")
                .name("tester1")
                .build();

        CheckingAccount senderAccount = CheckingAccount.of(senderAccountNumber, sender);
        CheckingAccount receiverAccount = CheckingAccount.of(receiverAccountNumber, Member.builder().build());

        RemitRequest request = RemitRequest.builder()
                .senderAccountNumber(senderAccountNumber)
                .receiverAccountNumber(receiverAccountNumber)
                .amount(12000L)
                .build();

        @Test
        @DisplayName("송신자의 송금 설정에 따라 송금한다. - 즉시 송금")
        void remit_immediately() {
            // given
            ReflectionTestUtils.setField(senderAccount, "balance", 12000L);

            Setting setting = Setting.create(sender);

            given(memberRepository.findByAccountNumber(senderAccountNumber))
                    .willReturn(Optional.of(sender));

            given(settingRepository.findByMember(sender))
                    .willReturn(Optional.of(setting));

            given(immediatelyRemitStrategy.remit(request, sender.getEmail()))
                    .willReturn(Pair.of(senderAccount, receiverAccount));

            given(transactionRepository.save(any()))
                    .willReturn(any());

            // when
            remitService.remit(request);

            // then
            verify(transactionRepository).save(any());
            verify(immediatelyRemitStrategy).remit(request, sender.getEmail());
        }

        @Test
        @DisplayName("송신자의 송금 설정에 따라 송금한다. - 대기 송금")
        void remit_pending() {
            // given
            ReflectionTestUtils.setField(senderAccount, "balance", 12000L);

            Setting setting = Setting.create(sender);
            setting.changeRemitTypeToPending();

            given(memberRepository.findByAccountNumber(senderAccountNumber))
                    .willReturn(Optional.of(sender));

            given(settingRepository.findByMember(sender))
                    .willReturn(Optional.of(setting));

            given(pendingRemitStrategy.remit(request, sender.getEmail()))
                    .willReturn(Pair.of(senderAccount, receiverAccount));

            given(transactionRepository.save(any()))
                    .willReturn(any());

            // when
            remitService.remit(request);

            // then
            verify(transactionRepository).save(any());
            verify(pendingRemitStrategy).remit(request, sender.getEmail());
        }

        @Test
        @DisplayName("송신자가 등록되어 있지 않은 회원이면 예외가 발생한다.")
        void remit_not_found_member() {
            // given
            given(memberRepository.findByAccountNumber(senderAccountNumber))
                    .willReturn(Optional.empty());

            // when
            // then
            assertThatThrownBy(() -> remitService.remit(request))
                    .isInstanceOf(TransactionException.class)
                    .hasMessage(NOT_FOUND_MEMBER.getMessage());
        }

        @Test
        @DisplayName("송신자의 설정을 찾을 수 없으면 예외가 발생한다.")
        void remit_not_found_setting() {
            // given
            given(memberRepository.findByAccountNumber(senderAccountNumber))
                    .willReturn(Optional.of(sender));

            given(settingRepository.findByMember(sender))
                    .willReturn(Optional.empty());

            // when
            // then
            assertThatThrownBy(() -> remitService.remit(request))
                    .isInstanceOf(TransactionException.class)
                    .hasMessage(NOT_FOUND_SETTING.getMessage());
        }
    }
}