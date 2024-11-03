package com.jindo.minipay.transaction.service.remit.strategy.impl;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.checking.service.charge.ChargeService;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.transaction.dto.RemitRequest;
import com.jindo.minipay.transaction.exception.TransactionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_ACCOUNT_NUMBER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class PendingRemitStrategyTest {
    @Mock
    CheckingAccountRepository checkingAccountRepository;

    @Mock
    ChargeService chargeService;

    @InjectMocks
    PendingRemitStrategy pendingRemitStrategy;

    String senderAccountNumber = "8888-01-1234567";
    String receiverAccountNumber = "8888-02-7654321";

    Member sender = Member.builder()
            .email("test@test.com")
            .password("test12345")
            .name("tester1")
            .build();

    Member receiver = Member.builder()
            .email("test2@test.com")
            .password("test12345")
            .name("tester2")
            .build();

    CheckingAccount senderAccount = CheckingAccount.of(senderAccountNumber, sender);
    CheckingAccount receiverAccount = CheckingAccount.of(receiverAccountNumber, receiver);

    RemitRequest request = RemitRequest.builder()
            .senderAccountNumber(senderAccountNumber)
            .receiverAccountNumber(receiverAccountNumber)
            .amount(12000L)
            .build();

    @Test
    @DisplayName("내 계좌를 확인하고, 친구에게 송금한다.")
    void remit() {
        // given
        ReflectionTestUtils.setField(senderAccount, "balance", 12000L);

        given(checkingAccountRepository.findByAccountNumber(senderAccountNumber))
                .willReturn(Optional.of(senderAccount));

        doNothing().when(chargeService)
                .autoChargingOrNot(senderAccount, sender.getEmail(), request.amount());

        given(checkingAccountRepository.findByAccountNumber(receiverAccountNumber))
                .willReturn(Optional.of(receiverAccount));

        // when
        Pair<CheckingAccount, CheckingAccount> pair =
                pendingRemitStrategy.remit(request, sender.getEmail());

        // then
        assertEquals(0L, pair.getFirst().getBalance());
        assertEquals(0L, pair.getSecond().getBalance());
    }

    @Test
    @DisplayName("내 계좌를 찾을 수 없으면 예외가 발생한다.")
    void remit_not_found_senderAccountNumber() {
        // given
        given(checkingAccountRepository.findByAccountNumber(senderAccountNumber))
                .willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> pendingRemitStrategy.remit(request, sender.getEmail()))
                .isInstanceOf(TransactionException.class)
                .hasMessageMatching(NOT_FOUND_ACCOUNT_NUMBER.getMessage());
    }

    @Test
    @DisplayName("친구 계좌를 찾을 수 없으면 예외가 발생한다.")
    void remit_not_found_receiverAccountNumber() {
        // given
        ReflectionTestUtils.setField(senderAccount, "balance", 12000L);

        given(checkingAccountRepository.findByAccountNumber(senderAccountNumber))
                .willReturn(Optional.of(senderAccount));

        doNothing().when(chargeService)
                .autoChargingOrNot(senderAccount, sender.getEmail(), request.amount());

        given(checkingAccountRepository.findByAccountNumber(receiverAccountNumber))
                .willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> pendingRemitStrategy.remit(request, sender.getEmail()))
                .isInstanceOf(TransactionException.class)
                .hasMessageMatching(NOT_FOUND_ACCOUNT_NUMBER.getMessage());
    }
}