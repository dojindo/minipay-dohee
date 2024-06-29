package com.jindo.minipay.account.common.util;

import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.type.AccountType;
import com.jindo.minipay.account.saving.repository.SavingAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccountNumberComponentTest {
    @Mock
    CheckingAccountRepository checkingAccountRepository;

    @Mock
    SavingAccountRepository savingAccountRepository;

    @InjectMocks
    AccountNumberComponent accountNumberComponent;

    @ParameterizedTest
    @EnumSource(AccountType.class)
    @DisplayName("계좌번호를 생성한다.")
    void getAccountNumber(AccountType accountType) {
        // given
        given(accountNumberComponent.getRepositoryEnumMap().get(accountType)
                .existsByAccountNumber(any()))
                .willReturn(false);

        // when
        String accountNumber =
                accountNumberComponent.getAccountNumber(accountType);

        // then
        assertThat(accountNumber).isNotEmpty();
        assertTrue(accountNumber.startsWith(String.valueOf(accountType.getCode())));
    }
}