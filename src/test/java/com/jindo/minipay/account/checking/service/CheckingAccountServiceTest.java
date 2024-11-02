package com.jindo.minipay.account.checking.service;

import com.jindo.minipay.account.checking.dto.ChargeRequest;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.checking.service.charge.ChargeService;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CheckingAccountServiceTest {
    @Mock
    CheckingAccountRepository accountRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    AccountNumberComponent accountNumberComponent;

    @Mock
    ChargeService chargeService;

    @InjectMocks
    CheckingAccountService accountService;

    String accountNumber = "8888-01-1234567";

    Member member = Member.builder()
            .email("test@test.com")
            .password("test12345")
            .name("tester1")
            .build();

    CheckingAccount account = CheckingAccount.of(accountNumber, member);

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

    @Test
    @DisplayName("메인 계좌에 충전한다.")
    void charge() {
        // given
        String accountNumber = "8888-01-1234567";
        ChargeRequest request = new ChargeRequest(accountNumber, 10000L);

        // when
        accountService.charge(request);

        // then
        verify(chargeService, times(1)).charge(request);
    }
}