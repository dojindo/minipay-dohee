package com.jindo.minipay.settlements.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jindo.minipay.settlements.dto.SettleAccountsRequest;
import com.jindo.minipay.settlements.dto.SettleAccountsResponse;
import com.jindo.minipay.settlements.dto.SettleCalculateRequest;
import com.jindo.minipay.settlements.dto.SettleCalculateResponse;
import com.jindo.minipay.settlements.service.SettlementService;
import com.jindo.minipay.settlements.type.SettlementStatus;
import com.jindo.minipay.settlements.type.SettlementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SettlementController.class)
class SettlementControllerTest {
    @MockBean
    SettlementService settlementService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("정산 금액을 계산한다.")
    void settleCalculate() throws Exception {
        // given
        SettleCalculateRequest request = SettleCalculateRequest.builder()
                .settlementType("DUTCH_PAY")
                .totalAmount(35000L)
                .numOfParticipants(2)
                .requesterId(1L)
                .build();

        SettleCalculateResponse response = SettleCalculateResponse.builder()
                .settlementType(SettlementType.DUTCH_PAY)
                .numOfParticipants(2)
                .totalAmount(35000L)
                .requestAmounts(List.of(17500L, 17500L))
                .remainingAmount(0)
                .build();

        given(settlementService.settleCalculate(any()))
                .willReturn(response);

        // when
        // then
        mockMvc.perform(post("/api/v1/settlements/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlementType")
                        .value("DUTCH_PAY"))
                .andExpect(jsonPath("$.numOfParticipants")
                        .value(2))
                .andExpect(jsonPath("$.totalAmount")
                        .value(35000))
                .andExpect(jsonPath("$.requestAmounts[0]")
                        .value(17500))
                .andExpect(jsonPath("$.remainingAmount")
                        .value(0))
                .andDo(print());
    }

    @Test
    @DisplayName("정산을 요청한다.")
    void settleAccounts() throws Exception {
        // given
        SettleAccountsRequest request = SettleAccountsRequest.builder()
                .settlementType("RANDOM")
                .totalAmount(35000L)
                .numOfParticipants(2)
                .requesterId(1L)
                .participants(List.of(
                        new SettleAccountsRequest
                                .ParticipantRequest(1L, 10000L),
                        new SettleAccountsRequest
                                .ParticipantRequest(2L, 25000L)))
                .remainingAmount(0)
                .build();

        SettleAccountsResponse response = SettleAccountsResponse.builder()
                .settlementType(SettlementType.RANDOM)
                .numOfParticipants(2)
                .totalAmount(35000L)
                .participants(List.of(
                        SettleAccountsResponse.ParticipantResponse.builder()
                                .participantId(1L)
                                .requestAmount(10000L)
                                .isRequester(true)
                                .settlementStatus(SettlementStatus.WAITING)
                                .build(),
                        SettleAccountsResponse.ParticipantResponse.builder()
                                .participantId(2L)
                                .requestAmount(25000L)
                                .isRequester(false)
                                .settlementStatus(SettlementStatus.WAITING)
                                .build()
                ))
                .remainingAmount(0)
                .settlementStatus(SettlementStatus.WAITING)
                .build();

        given(settlementService.settleAccounts(request))
                .willReturn(response);

        // when
        // then
        mockMvc.perform(post("/api/v1/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlementType")
                        .value("RANDOM"))
                .andExpect(jsonPath("$.totalAmount")
                        .value(35000))
                .andExpect(jsonPath("$.numOfParticipants")
                        .value(2))
                .andExpect(jsonPath("$.participants.size()")
                        .value(2))
                .andExpect(jsonPath("$.remainingAmount")
                        .value(0))
                .andExpect(jsonPath("$.settlementStatus")
                        .value("WAITING"))
                .andDo(print());
    }
}