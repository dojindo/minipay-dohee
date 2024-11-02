package com.jindo.minipay.transaction.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionStatus {
    PENDING("대기"),
    COMPLETE("완료"),
    FAILED("실패");

    private final String description;
}
