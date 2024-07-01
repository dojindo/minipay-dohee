package com.jindo.minipay.account.saving.service;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.account.common.type.AccountType;
import com.jindo.minipay.account.common.util.AccountNumberComponent;
import com.jindo.minipay.account.saving.dto.CreateSavingAccountRequest;
import com.jindo.minipay.account.saving.dto.PayInRequest;
import com.jindo.minipay.account.saving.dto.PayInResponse;
import com.jindo.minipay.account.saving.entity.SavingAccount;
import com.jindo.minipay.account.saving.repository.SavingAccountRepository;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.jindo.minipay.global.exception.ErrorCode.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SavingAccountServiceTest {
    @Mock
    SavingAccountRepository accountRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    AccountNumberComponent accountNumberComponent;

    @InjectMocks
    SavingAccountService accountService;

    String accountNumber = "8800-01-1234567";

    Member member = Member.builder()
            .email("test@test.com")
            .password("test12345")
            .name("tester1")
            .build();

    @Nested
    @DisplayName("적금 계좌 생성 메서드")
    class CreateAccountMethod {
        SavingAccount account = SavingAccount.of(accountNumber, member);

        CreateSavingAccountRequest request =
                new CreateSavingAccountRequest(1L);

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(account, "id", 1L);
        }

        @Test
        @DisplayName("적금 계좌를 생성한다.")
        void createAccount() {
            // given
            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(member));

            given(accountNumberComponent.getAccountNumber(AccountType.SAVINGS))
                    .willReturn(accountNumber);

            given(accountRepository.save(any()))
                    .willReturn(account);

            // when
            Long accountId = accountService.createAccount(request);

            // then
            assertEquals(1L, accountId);
            verify(accountRepository, times(1))
                    .save(any());
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 에외가 발생한다.")
        void createAccount_notFount_member() {
            given(memberRepository.findById(1L))
                    .willReturn(Optional.empty());

            // when
            // then
            assertThatThrownBy(() -> accountService.createAccount(request))
                    .isInstanceOf(AccountException.class)
                    .hasMessageContaining(NOT_FOUND_MEMBER.getMessage());
        }
    }

    @Nested
    @DisplayName("적금 계좌 납입 메서드")
    class PayInMethod {
        SavingAccount savingAccount = SavingAccount.of(accountNumber, member);

        CheckingAccount checkingAccount =
                CheckingAccount.of("8800-01-1234567", member);

        PayInRequest request = new PayInRequest(accountNumber,
                "8888-01-1234567", 10000L);

        @Test
        @DisplayName("적금 계좌에 납입한다.")
        void payIn() {
            // given
            ReflectionTestUtils.setField(checkingAccount, "balance", 10000L);
            ReflectionTestUtils.setField(member, "checkingAccount", checkingAccount);

            given(accountRepository.findByAccountNumberFetchJoin(accountNumber))
                    .willReturn(Optional.of(savingAccount));

            // when
            PayInResponse response = accountService.payIn(request);

            // then
            assertEquals(10000L, response.amount());
        }

        @Test
        @DisplayName("존재하지 않는 적금 계좌이면 예외가 발생한다.")
        void payIn_notFound_accountNumber() {
            // given
            given(accountRepository.findByAccountNumberFetchJoin(accountNumber))
                    .willReturn(Optional.empty());

            // when
            // then
            assertThatThrownBy(() -> accountService.payIn(request))
                    .isInstanceOf(AccountException.class)
                    .hasMessageContaining(NOT_FOUND_ACCOUNT_NUMBER.getMessage());
        }

        @Test
        @DisplayName("메인 계좌 잔액이 부족하면 예외가 발생한다.")
        void payIn_insufficient_balance() {
            // given
            ReflectionTestUtils.setField(checkingAccount, "balance", 5000L);
            ReflectionTestUtils.setField(member, "checkingAccount", checkingAccount);

            given(accountRepository.findByAccountNumberFetchJoin(accountNumber))
                    .willReturn(Optional.of(savingAccount));

            // when
            // then
            assertThatThrownBy(() -> accountService.payIn(request))
                    .isInstanceOf(AccountException.class)
                    .hasMessageContaining(INSUFFICIENT_BALANCE.getMessage());
        }
    }
}