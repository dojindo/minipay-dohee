package com.jindo.minipay.settlements.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jindo.minipay.global.annotation.ValidEnum;
import com.jindo.minipay.settlements.type.SettlementType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Builder
@AllArgsConstructor
@Getter
public class SettleCalculateRequest {
    @NotBlank @ValidEnum(target = SettlementType.class)
    private String settlementType;

    @NotNull @Min(1) @Max(5_000_000)
    private Long totalAmount;

    @NotNull @Min(1) @Max(50)
    private Integer numOfParticipants; // 요청자 포함

    @NotNull
    private Long requesterId;

    @JsonIgnore
    private SettlementType settlementTypeEnum;

    public SettlementType getSettlementType() {
        if (settlementTypeEnum == null) {
            setSettlementType();
        }
        return settlementTypeEnum;
    }

    private void setSettlementType() {
        if (StringUtils.hasText(settlementType)) {
            settlementTypeEnum = SettlementType.of(settlementType);
        }
    }
}
