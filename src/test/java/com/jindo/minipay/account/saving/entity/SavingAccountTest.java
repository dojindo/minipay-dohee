package com.jindo.minipay.account.saving.entity;

import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class SavingAccountTest {
    @Test
    @DisplayName("금액이 음수이면 예외가 발생한다.")
    void increaseBalance() {
        // given
        SavingAccount savingAccount = new SavingAccount();
        long amount = -1L;

        // when
        // then
        assertThatThrownBy(() -> savingAccount.increaseAmount(amount))
                .isInstanceOf(AccountException.class)
                .hasMessageContaining(ErrorCode.INVALID_REQUEST.getMessage());
    }
}