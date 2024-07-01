package com.jindo.minipay.account.saving.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SavingType {
    REGULAR("정기 적금"),
    FREE("자유 적금");

    private final String description;
}
