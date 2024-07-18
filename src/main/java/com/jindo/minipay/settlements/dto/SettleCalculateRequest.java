package com.jindo.minipay.settlements.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jindo.minipay.global.annotation.ValidEnum;
import com.jindo.minipay.settlements.type.SettlementType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record SettleCalculateRequest(
        @NotBlank @ValidEnum(target = SettlementType.class)
        String settlementType,

        @NonNull @Min(1) @Max(5_000_000)
        Long totalAmount,

        @NonNull @Min(1) @Max(50)
        Integer numOfParticipants,

        @NotNull
        Long requesterId,

        @JsonIgnore
        SettlementType settleTypeEnum
) {
    public SettleCalculateRequest {
        settleTypeEnum = SettlementType.of(settlementType);
    }
}
