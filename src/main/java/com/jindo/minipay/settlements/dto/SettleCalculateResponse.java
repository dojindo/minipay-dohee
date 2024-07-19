package com.jindo.minipay.settlements.dto;

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

        return SettleCalculateResponse.builder()
                .settlementType(settlementType)
                .numOfParticipants(numOfParticipants)
                .totalAmount(totalAmount)
                .requestAmounts(requestAmounts)
                .remainingAmount(remainingAmount)
                .build();
    }
}
