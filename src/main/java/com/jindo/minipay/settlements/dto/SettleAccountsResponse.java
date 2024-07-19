package com.jindo.minipay.settlements.dto;

import com.jindo.minipay.settlements.entity.Settlement;
import com.jindo.minipay.settlements.entity.SettlementParticipant;
import com.jindo.minipay.settlements.type.SettlementStatus;
import com.jindo.minipay.settlements.type.SettlementType;
import lombok.Builder;

import java.util.List;

@Builder
public record SettleAccountsResponse(
        SettlementType settlementType,
        int numOfParticipants,
        long totalAmount,
        List<ParticipantResponse> participants,
        long remainingAmount,
        SettlementStatus settlementStatus
) {
    public static SettleAccountsResponse from(Settlement settlement) {
        return SettleAccountsResponse.builder()
                .settlementType(settlement.getSettlementType())
                .numOfParticipants(settlement.getNumOfParticipants())
                .totalAmount(settlement.getTotalAmount())
                .participants(settlement.getParticipants().stream()
                        .map(ParticipantResponse::from)
                        .toList())
                .remainingAmount(settlement.getRemainingAmount())
                .settlementStatus(settlement.getSettlementStatus())
                .build();
    }

    @Builder
    public record ParticipantResponse(
            Long participantId,
            long requestAmount,
            boolean isRequester,
            SettlementStatus settlementStatus
    ) {
        public static ParticipantResponse from(SettlementParticipant participant) {
            return ParticipantResponse.builder()
                    .participantId(participant.getId())
                    .requestAmount(participant.getRequestAmount())
                    .isRequester(participant.isRequester())
                    .settlementStatus(participant.getSettlementStatus())
                    .build();
        }
    }
}
