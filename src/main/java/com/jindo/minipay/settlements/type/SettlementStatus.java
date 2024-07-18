package com.jindo.minipay.settlements.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementStatus {
    WAITING("정산 대기"),
    SETTLING("정산 중"),
    COMPLETE("정산 완료");

    private final String descriptor;
}
