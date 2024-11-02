package com.jindo.minipay.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jindo.minipay.transaction.dto.RemitRequest;
import com.jindo.minipay.transaction.dto.RemitResponse;
import com.jindo.minipay.transaction.service.TransactionService;
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

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    @MockBean
    TransactionService transactionService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("메인 계좌에서 친구 계좌로 송금한다.")
    void remit() throws Exception {
        // given
        String senderAccountNumber = "8888-01-1234567";

        RemitRequest request = RemitRequest.builder()
                .senderAccountNumber(senderAccountNumber)
                .receiverAccountNumber("8888-02-7654321")
                .amount(10000L)
                .build();

        RemitResponse response = new RemitResponse(senderAccountNumber, 5000L);

        given(transactionService.remit(request)).willReturn(response);

        // when
        // then
        mockMvc.perform(post("/api/v1/transactions/remit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(senderAccountNumber))
                .andExpect(jsonPath("$.balance").value(5000L))
                .andDo(print());
    }
}