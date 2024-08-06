package com.jindo.minipay.account.checking.service;

import com.jindo.minipay.account.checking.dto.ChargeRequest;
import com.jindo.minipay.account.checking.dto.ChargeResponse;
import com.jindo.minipay.account.checking.dto.RemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.account.common.type.AccountType;
import com.jindo.minipay.account.common.util.AccountNumberComponent;
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
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.jindo.minipay.global.exception.ErrorCode.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckingAccountServiceTest {
    @Mock
    CheckingAccountRepository accountRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    AccountNumberComponent accountNumberComponent;

    @Mock
    ValueOperations<String, Object> redisValueOps;

    @InjectMocks
    CheckingAccountService accountService;

    String accountNumber = "8888-01-1234567";

    Member member = Member.builder()
            .email("test@test.com")
            .password("test12345")
            .name("tester1")
            .build();

    CheckingAccount account = CheckingAccount.of(accountNumber, member);

    String CHARGE_KEY_PREFIX = "CHARGE:";

    @Nested
    @DisplayName("메인 계좌 생성 메서드")
    class CreateAccountMethod {
        Long memberId = 1L;

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(account, "id", 1L);
        }

        @Test
        @DisplayName("메인 계좌를 생성한다.")
        void createAccount() {
            // given
            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(member));

            given(accountNumberComponent.getAccountNumber(AccountType.CHECKING))
                    .willReturn(accountNumber);

            given(accountRepository.save(any()))
                    .willReturn(account);

            // when
            accountService.createAccount(memberId);

            // then
            verify(accountRepository, times(1))
                    .save(any());
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 에외가 발생한다.")
        void createAccount_notFount_member() {
            // given
            given(memberRepository.findById(1L))
                    .willReturn(Optional.empty());

            // when
            // then
            assertThatThrownBy(() -> accountService.createAccount(memberId))
                    .isInstanceOf(AccountException.class)
                    .hasMessageContaining(NOT_FOUND_MEMBER.getMessage());
        }
    }

    @Nested
    class ChargeMethod {
        String accountNumber = "8888-01-1234567";
        ChargeRequest request = new ChargeRequest(accountNumber, 10000L);

        @Test
        @DisplayName("메인 계좌에 충전한다.")
        void charge() {
            // given
            given(accountRepository.findByAccountNumberFetchJoin(accountNumber))
                    .willReturn(Optional.of(account));

            given(redisValueOps.get(CHARGE_KEY_PREFIX + member.getEmail()))
                    .willReturn(null);

            doNothing().when(redisValueOps)
                    .set(any(), any(), anyLong(), any());

            // when
            ChargeResponse response = accountService.charge(request);

            // then
            assertEquals(10000, response.balance());
            verify(redisValueOps, times(1))
                    .set(any(), any(), anyLong(), any());
        }

        @Test
        @DisplayName("없는 계좌번호이면 예외가 발생한다.")
        void charge_notFount_accountNumber() {
            // given
            given(accountRepository.findByAccountNumberFetchJoin(accountNumber))
                    .willReturn(Optional.empty());

            // when
            // then
            assertThatThrownBy(() -> accountService.charge(request))
                    .isInstanceOf(AccountException.class)
                    .hasMessageContaining(NOT_FOUND_ACCOUNT_NUMBER.getMessage());
        }

        @Test
        @DisplayName("1일 충전 한도를 초과하면 예외가 발생한다.")
        void charge_exceeded_charging_limit() {
            // given
            Integer accumulatedAmount = 2_990_001;

            given(accountRepository.findByAccountNumberFetchJoin(accountNumber))
                    .willReturn(Optional.of(account));

            given(redisValueOps.get(CHARGE_KEY_PREFIX + member.getEmail()))
                    .willReturn(accumulatedAmount);

            // when
            // then
            assertThatThrownBy(() -> accountService.charge(request))
                    .isInstanceOf(AccountException.class)
                    .hasMessageContaining(EXCEEDED_DAILY_CHARGING_LIMIT.getMessage());
        }
    }

    @Nested
    @DisplayName("송금 메서드")
    class RemitMethod {
        String myAccountNumber = "8888-01-1234567";
        String receiverAccountNumber = "8888-02-7654321";

        RemitRequest request = RemitRequest.builder()
                .myAccountNumber(myAccountNumber)
                .receiverAccountNumber(receiverAccountNumber)
                .amount(12000L)
                .build();

        CheckingAccount receiverCheckingAccount =
                CheckingAccount.of(receiverAccountNumber, null);

        @Test
        @DisplayName("내 계좌를 확인하고, 친구에게 송금한다.")
        void remit() {
            // given
            ReflectionTestUtils.setField(account, "balance", 12000L);

            given(accountRepository.findByAccountNumberFetchJoin(accountNumber))
                    .willReturn(Optional.of(account));

            given(accountRepository.findByAccountNumber(receiverAccountNumber))
                    .willReturn(Optional.of(receiverCheckingAccount));

            // when
            RemitResponse response = accountService.remit(request);

            // then
            assertEquals(0, response.balance());
            verify(redisValueOps, times(0)).get(any());
        }

        @Test
        @DisplayName("내 계좌 잔액 확인 시, 부족한 경우 자동 충전한다.")
        void remit_autoCharging() {
            // given
            given(accountRepository.findByAccountNumberFetchJoin(accountNumber))
                    .willReturn(Optional.of(account));

            given(accountRepository.findByAccountNumber(receiverAccountNumber))
                    .willReturn(Optional.of(receiverCheckingAccount));

            given(redisValueOps.get(CHARGE_KEY_PREFIX + member.getEmail()))
                    .willReturn(null);

            doNothing().when(redisValueOps)
                    .set(any(), any(), anyLong(), any());

            // when
            RemitResponse response = accountService.remit(request);

            // then
            assertEquals(8000, response.balance());
            verify(redisValueOps, times(1))
                    .set(any(), any(), anyLong(), any());
        }

        @Test
        @DisplayName("내 계좌를 찾을 수 없으면 예외가 발생한다.")
        void remit_notFount_myAccountNumber() {
            // given
            given(accountRepository.findByAccountNumberFetchJoin(accountNumber))
                    .willReturn(Optional.empty());

            // when
            // then
            assertThatThrownBy(() -> accountService.remit(request))
                    .isInstanceOf(AccountException.class)
                    .hasMessageMatching(NOT_FOUND_ACCOUNT_NUMBER.getMessage());
        }

        @Test
        @DisplayName("친구 계좌를 찾을 수 없으면 예외가 발생한다.")
        void remit_notFount_receiverAccountNumber() {
            // given
            given(accountRepository.findByAccountNumberFetchJoin(accountNumber))
                    .willReturn(Optional.of(account));

            given(accountRepository.findByAccountNumber(receiverAccountNumber))
                    .willReturn(Optional.empty());

            // when
            // then
            assertThatThrownBy(() -> accountService.remit(request))
                    .isInstanceOf(AccountException.class)
                    .hasMessageMatching(NOT_FOUND_ACCOUNT_NUMBER.getMessage());
        }
    }
}