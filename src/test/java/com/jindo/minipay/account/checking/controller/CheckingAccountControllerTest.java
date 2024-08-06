package com.jindo.minipay.account.checking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jindo.minipay.account.checking.dto.ChargeRequest;
import com.jindo.minipay.account.checking.dto.ChargeResponse;
import com.jindo.minipay.account.checking.dto.RemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import com.jindo.minipay.account.checking.service.CheckingAccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckingAccountController.class)
class CheckingAccountControllerTest {
    @MockBean
    CheckingAccountService accountService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("메인 계좌에 충전한다.")
    void charge() throws Exception {
        // given
        String accountNumber = "8888-01-1234567";
        ChargeRequest request = new ChargeRequest(accountNumber, 10000L);
        ChargeResponse response = new ChargeResponse(accountNumber, 10000L);

        given(accountService.charge(request))
                .willReturn(response);

        // when
        // then
        mockMvc.perform(post("/api/v1/account/checking/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.balance").value(10000))
                .andDo(print());
    }

    @Test
    @DisplayName("메인 계좌에서 친구 계좌로 송금한다.")
    void remit() throws Exception {
        // given
        String myAccountNumber = "8888-01-1234567";

        RemitRequest request = RemitRequest.builder()
                .myAccountNumber(myAccountNumber)
                .receiverAccountNumber("8888-02-7654321")
                .amount(10000L)
                .build();

        RemitResponse response = new RemitResponse(myAccountNumber, 5000L);

        given(accountService.remit(request)).willReturn(response);

        // when
        // then
        mockMvc.perform(post("/api/v1/account/checking/remit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(myAccountNumber))
                .andExpect(jsonPath("$.balance").value(5000L))
                .andDo(print());
    }
}