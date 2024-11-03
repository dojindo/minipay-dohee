package com.jindo.minipay.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jindo.minipay.transaction.dto.RemitRequest;
import com.jindo.minipay.transaction.dto.RemitResponse;
import com.jindo.minipay.transaction.exception.TransactionException;
import com.jindo.minipay.transaction.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_ACCOUNT_NUMBER;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
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

    @Nested
    @DisplayName("송금하기 메서드")
    class RemitMethod {
        String senderAccountNumber = "8888-01-1234567";

        RemitRequest request = RemitRequest.builder()
                .senderAccountNumber(senderAccountNumber)
                .receiverAccountNumber("8888-02-7654321")
                .amount(10000L)
                .build();

        @Test
        @DisplayName("메인 계좌에서 친구 계좌로 송금한다.")
        void remit() throws Exception {
            // given
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

        @Test
        @DisplayName("메인 계좌에서 친구 계좌로 송금 시 실패하면 예외를 응답한다.")
        void remit_failed() throws Exception {
            // given
            given(transactionService.remit(request))
                    .willThrow(new TransactionException(NOT_FOUND_ACCOUNT_NUMBER));

            // when
            // then
            mockMvc.perform(post("/api/v1/transactions/remit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError())
                    .andExpect(jsonPath("$.errorCode")
                            .value(NOT_FOUND_ACCOUNT_NUMBER.name()))
                    .andExpect(jsonPath("$.errorMessage")
                            .value(NOT_FOUND_ACCOUNT_NUMBER.getMessage()))
                    .andDo(print());

            verify(transactionService).saveRemitFailed(request);
        }
    }
}