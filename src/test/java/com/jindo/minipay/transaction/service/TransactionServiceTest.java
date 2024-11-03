package com.jindo.minipay.transaction.service;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.transaction.dto.RemitRequest;
import com.jindo.minipay.transaction.exception.TransactionException;
import com.jindo.minipay.transaction.repository.TransactionRepository;
import com.jindo.minipay.transaction.service.remit.RemitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_ACCOUNT_NUMBER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    RemitService remitService;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    CheckingAccountRepository checkingAccountRepository;

    @InjectMocks
    TransactionService transactionService;

    @Nested
    @DisplayName("송금 메서드")
    class RemitMethod {
        String senderAccountNumber = "8888-01-1234567";
        String receiverAccountNumber = "8888-02-7654321";

        RemitRequest request = RemitRequest.builder()
                .senderAccountNumber(senderAccountNumber)
                .receiverAccountNumber(receiverAccountNumber)
                .amount(12000L)
                .build();

        CheckingAccount senderAccount = CheckingAccount.of(senderAccountNumber,
                Member.builder().build());

        CheckingAccount receiverAccount = CheckingAccount.of(receiverAccountNumber,
                Member.builder().build());

        @Test
        @DisplayName("친구 계좌로 송금한다.")
        void remit() {
            // given
            // when
            transactionService.remit(request);

            // then
            verify(remitService, times(1)).remit(request);
        }

        @Test
        @DisplayName("송금 실패 시 거래 실패로 상태를 저장한다.")
        void saveRemitFailed() {
            // given
            given(checkingAccountRepository.findByAccountNumber(senderAccountNumber))
                    .willReturn(Optional.of(senderAccount));

            given(checkingAccountRepository.findByAccountNumber(receiverAccountNumber))
                    .willReturn(Optional.of(receiverAccount));

            given(transactionRepository.save(any()))
                    .willReturn(any());

            // when
            transactionService.saveRemitFailed(request);

            // then
            verify(transactionRepository).save(any());
        }

        @Test
        @DisplayName("송신자 계좌를 찾을 수 없으면 예외가 발생한다.")
        void saveRemitFailed_not_found_senderAccount() {
            // given
            given(checkingAccountRepository.findByAccountNumber(senderAccountNumber))
                    .willReturn(Optional.empty());

            // when
            // then
            assertThatThrownBy(() -> transactionService.saveRemitFailed(request))
                    .isInstanceOf(TransactionException.class)
                    .hasMessage(NOT_FOUND_ACCOUNT_NUMBER.getMessage());
        }

        @Test
        @DisplayName("수신자 계좌를 찾을 수 없으면 예외가 발생한다.")
        void saveRemitFailed_not_found_receiverAccount() {
            // given
            given(checkingAccountRepository.findByAccountNumber(senderAccountNumber))
                    .willReturn(Optional.of(senderAccount));

            given(checkingAccountRepository.findByAccountNumber(receiverAccountNumber))
                    .willReturn(Optional.empty());

            // when
            // then
            assertThatThrownBy(() -> transactionService.saveRemitFailed(request))
                    .isInstanceOf(TransactionException.class)
                    .hasMessage(NOT_FOUND_ACCOUNT_NUMBER.getMessage());
        }
    }
}