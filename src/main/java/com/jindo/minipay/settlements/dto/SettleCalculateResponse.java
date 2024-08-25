package com.jindo.minipay.settlements.dto;

import com.jindo.minipay.global.exception.ErrorCode;
import com.jindo.minipay.settlements.exception.SettlementException;
import com.jindo.minipay.settlements.type.SettlementType;
import lombok.Builder;

import java.util.List;

@Builder
public record SettleCalculateResponse(
        SettlementType settlementType,
        int numOfParticipants,
        long totalAmount,
        List<Long> requestAmounts,
        long remainingAmount
) {
    public static SettleCalculateResponse of(SettlementType settlementType,
                                             int numOfParticipants,
                                             long totalAmount,
                                             List<Long> requestAmounts) {
        long remainingAmount = 0;
        if (settlementType == SettlementType.DUTCH_PAY) {
            remainingAmount = totalAmount % numOfParticipants;
        }

        long sumOfRequest = requestAmounts.stream().mapToLong(o -> o).sum();
        if (sumOfRequest + remainingAmount != totalAmount) {
            throw new SettlementException(ErrorCode.INCORRECT_TOTAL_AMOUNT);
        }

        return SettleCalculateResponse.builder()
                .settlementType(settlementType)
                .numOfParticipants(numOfParticipants)
                .totalAmount(totalAmount)
                .requestAmounts(requestAmounts)
                .remainingAmount(remainingAmount)
                .build();
    }
}
