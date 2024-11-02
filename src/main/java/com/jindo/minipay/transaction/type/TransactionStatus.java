package com.jindo.minipay.transaction.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionStatus {
    PENDING("대기"),
    COMPLETE("완료"),
    CANCEL("취소");

    private final String description;
}
