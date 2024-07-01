package com.jindo.minipay.account.common.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountType {
    CHECKING("입출금 계좌", 8888),
    SAVINGS("적금 걔좌", 8800);

    private final String description;
    private final Integer code;
}
