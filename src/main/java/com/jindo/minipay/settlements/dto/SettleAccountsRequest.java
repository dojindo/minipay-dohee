package com.jindo.minipay.settlements.dto;

import com.jindo.minipay.global.annotation.ValidEnum;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.settlements.entity.Settlement;
import com.jindo.minipay.settlements.type.SettlementType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder
public record SettleAccountsRequest(
        @NotBlank @ValidEnum(target = SettlementType.class)
        String settlementType,

        @NonNull @Min(1) @Max(5_000_000)
        Long totalAmount,

        @NonNull @Min(1) @Max(50)
        Integer numOfParticipants,

        @NotNull
        Long requesterId,

        @NotNull @Size(min=1, max=50)
        @Valid
        List<ParticipantRequest> participants, // 요청자 포함

        long remainingAmount
) {
    public Settlement toEntity(Member requester) {
        return Settlement.builder()
                .numOfParticipants(numOfParticipants)
                .totalAmount(totalAmount)
                .remainingAmount(remainingAmount)
                .settlementType(SettlementType.of(settlementType))
                .requester(requester)
                .build();
    }

    public record ParticipantRequest(
            @NotNull
            Long participantId,

            @NotNull @PositiveOrZero
            Long requestAmount
    ) {
    }
}
