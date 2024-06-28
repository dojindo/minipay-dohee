package com.jindo.minipay.account.saving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jindo.minipay.account.saving.dto.CreateSavingAccountRequest;
import com.jindo.minipay.account.saving.dto.PayInRequest;
import com.jindo.minipay.account.saving.dto.PayInResponse;
import com.jindo.minipay.account.saving.service.SavingAccountService;
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

@WebMvcTest(SavingAccountController.class)
class SavingAccountControllerTest {
    @MockBean
    SavingAccountService savingAccountService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("적금 계좌를 생성한다.")
    void create() throws Exception {
        // given
        CreateSavingAccountRequest request = new CreateSavingAccountRequest(1L);

        given(savingAccountService.createAccount(request))
                .willReturn(1L);

        // when
        // then
        mockMvc.perform(post("/api/v1/account/saving")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @DisplayName("적금 계좌에 납입한다.")
    void payIn() throws Exception {
        // given
        String accountNumber = "8800-01-1234567";

        PayInRequest request = new PayInRequest(accountNumber,
                "8888-01-1234567", 10000L);

        PayInResponse response = new PayInResponse(accountNumber, 10000L);

        given(savingAccountService.payIn(request))
                .willReturn(response);

        // when
        // then
        mockMvc.perform(post("/api/v1/account/saving/payin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.amount").value(10000))
                .andDo(print());
    }
}