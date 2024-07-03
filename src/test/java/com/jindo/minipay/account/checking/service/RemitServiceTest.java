package com.jindo.minipay.account.checking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jindo.minipay.account.checking.dto.RemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.common.exception.AccountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@WebMvcTest(RemitService.class)
class RemitServiceTest {
    @MockBean
    CheckingAccountService checkingAccountService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private RemitService remitService;

    String myAccountNumber = "8888-01-1234567";

    RemitRequest request = RemitRequest.builder()
            .myAccountNumber(myAccountNumber)
            .receiverAccountNumber("8888-02-7654321")
            .amount(10000L)
            .build();

    CheckingAccount myCheckingAccount =
            CheckingAccount.of(myAccountNumber, null);

    @Test
    @DisplayName("내 계좌를 확인하고, 친구에게 송금한다.")
    void remit() {
        // given
        given(checkingAccountService.checkMyAccount(request))
                .willReturn(myCheckingAccount);

        doNothing().when(checkingAccountService).sendMoneyToReceiver(request);

        // when
        RemitResponse response = remitService.remit(request);

        // then
        assertEquals(0, response.balance());
    }

    @Test
    @DisplayName("내 계좌 확인 시 예외가 발생하면, 친구에게 송금하지 않는다.")
    void remit_checkMyAccount_willThrow() {
        // given
        given(checkingAccountService.checkMyAccount(request))
                .willThrow(AccountException.class);

        // when
        // then
        assertThatThrownBy(() -> remitService.remit(request))
                .isInstanceOf(AccountException.class);

        verify(checkingAccountService, times(0))
                .sendMoneyToReceiver(any());
    }

    @Test
    @DisplayName("친구 계좌 확인 시 예외가 발생하면, 송금하지 않는다.")
    void remit_sendMoneyToReceiver_willThrow() {
        // given
        given(checkingAccountService.checkMyAccount(request))
                .willReturn(myCheckingAccount);

        doThrow(AccountException.class).when(checkingAccountService)
                .sendMoneyToReceiver(request);

        // when
        // then
        assertThatThrownBy(() -> remitService.remit(request))
                .isInstanceOf(AccountException.class);
    }
}