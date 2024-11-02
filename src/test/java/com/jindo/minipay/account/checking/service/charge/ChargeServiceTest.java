package com.jindo.minipay.account.checking.service.charge;

import com.jindo.minipay.account.checking.dto.ChargeRequest;
import com.jindo.minipay.account.checking.dto.ChargeResponse;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static com.jindo.minipay.global.exception.ErrorCode.EXCEEDED_DAILY_CHARGING_LIMIT;
import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_ACCOUNT_NUMBER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargeServiceTest {
    @Mock
    CheckingAccountRepository checkingAccountRepository;

    @Mock
    ValueOperations<String, Object> redisValueOps;

    @InjectMocks
    ChargeService chargeService;

    String accountNumber = "8888-01-1234567";

    Member member = Member.builder()
            .email("test@test.com")
            .password("test12345")
            .name("tester1")
            .build();

    CheckingAccount checkingAccount = CheckingAccount.of(accountNumber, member);

    private static final String CHARGE_KEY_PREFIX = "CHARGE:";

    @Nested
    @DisplayName("충전하기 메서드")
    class ChargeMethod {
        ChargeRequest request = new ChargeRequest(accountNumber, 10000L);

        @Test
        @DisplayName("메인 계좌에 충전한다.")
        void charge() {
            // given
            given(checkingAccountRepository.findByAccountNumberFetchJoin(accountNumber))
                    .willReturn(Optional.of(checkingAccount));

            given(redisValueOps.get(CHARGE_KEY_PREFIX + member.getEmail()))
                    .willReturn(null);

            doNothing().when(redisValueOps)
                    .set(any(), any(), anyLong(), any());

            // when
            ChargeResponse response = chargeService.charge(request);

            // then
            assertEquals(10000, response.balance());
            verify(redisValueOps, times(1))
                    .set(any(), any(), anyLong(), any());
        }

        @Test
        @DisplayName("없는 계좌번호이면 예외가 발생한다.")
        void charge_notFount_accountNumber() {
            // given
            given(checkingAccountRepository.findByAccountNumberFetchJoin(accountNumber))
                    .willReturn(Optional.empty());

            // when
            // then
            assertThatThrownBy(() -> chargeService.charge(request))
                    .isInstanceOf(AccountException.class)
                    .hasMessage(NOT_FOUND_ACCOUNT_NUMBER.getMessage());
        }

        @Test
        @DisplayName("1일 충전 한도를 초과하면 예외가 발생한다.")
        void charge_exceeded_charging_limit() {
            // given
            Integer accumulatedAmount = 2_990_001;

            given(checkingAccountRepository.findByAccountNumberFetchJoin(accountNumber))
                    .willReturn(Optional.of(checkingAccount));

            given(redisValueOps.get(CHARGE_KEY_PREFIX + member.getEmail()))
                    .willReturn(accumulatedAmount);

            // when
            // then
            assertThatThrownBy(() -> chargeService.charge(request))
                    .isInstanceOf(AccountException.class)
                    .hasMessage(EXCEEDED_DAILY_CHARGING_LIMIT.getMessage());
        }
    }

    @Nested
    @DisplayName("자동 충전 메서드")
    class autoChargeMethod {
        @Test
        @DisplayName("계좌의 잔액이 부족한 경우 자동 충전한다.")
        void autoChargingOrNot() {
            // given
            Long amount = 12000L;

            given(redisValueOps.get(CHARGE_KEY_PREFIX + member.getEmail()))
                    .willReturn(null);

            doNothing().when(redisValueOps)
                    .set(any(), any(), anyLong(), any());

            // when
            chargeService.autoChargingOrNot(checkingAccount, member.getEmail(), amount);

            // then
            assertEquals(20000, checkingAccount.getBalance());
            verify(redisValueOps, times(1))
                    .set(any(), any(), anyLong(), any());
        }

        @Test
        @DisplayName("자동 충전 시 1일 충전 한도를 초과하면 예외가 발생한다.")
        void autoChargingOrNot_exceeded_charging_limit() {
            Long amount = 12000L;

            given(redisValueOps.get(CHARGE_KEY_PREFIX + member.getEmail()))
                    .willReturn(3_000_000);

            // when
            // then
            assertThatThrownBy(() ->
                    chargeService.autoChargingOrNot(checkingAccount, member.getEmail(), amount))
                    .isInstanceOf(AccountException.class)
                    .hasMessage(EXCEEDED_DAILY_CHARGING_LIMIT.getMessage());
        }
    }
}