package com.jindo.minipay.account.checking.entity;

import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class CheckingAccountTest {
    @Test
    @DisplayName("금액이 음수이면 예외가 발생한다.")
    void increaseBalance() {
        // given
        CheckingAccount checkingAccount = new CheckingAccount();
        long amount = -1L;

        // when
        // then
        assertThatThrownBy(() -> checkingAccount.increaseBalance(amount))
                .isInstanceOf(AccountException.class)
                .hasMessageContaining(ErrorCode.INVALID_REQUEST.getMessage());
    }

    @Test
    @DisplayName("금액이 잔액보다 많으면 예외가 발생한다.")
    void decreaseBalance() {
        // given
        CheckingAccount checkingAccount = new CheckingAccount();
        long amount = 1000L;

        // when
        // then
        assertThatThrownBy(() -> checkingAccount.decreaseBalance(amount))
                .isInstanceOf(AccountException.class)
                .hasMessageContaining(ErrorCode.INSUFFICIENT_BALANCE.getMessage());
    }
}