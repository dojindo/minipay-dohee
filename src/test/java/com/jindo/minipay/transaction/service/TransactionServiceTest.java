package com.jindo.minipay.transaction.service;

import com.jindo.minipay.transaction.dto.RemitRequest;
import com.jindo.minipay.transaction.service.remit.RemitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    RemitService remitService;

    @InjectMocks
    TransactionService transactionService;

    @Test
    @DisplayName("친구 계좌로 송금한다.")
    void remit() {
        // given
        String senderAccountNumber = "8888-01-1234567";
        String receiverAccountNumber = "8888-02-7654321";

        RemitRequest request = RemitRequest.builder()
                .senderAccountNumber(senderAccountNumber)
                .receiverAccountNumber(receiverAccountNumber)
                .amount(12000L)
                .build();

        // when
        transactionService.remit(request);

        // then
        verify(remitService, times(1)).remit(request);
    }
}