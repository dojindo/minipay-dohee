package com.jindo.minipay.setting.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RemitSettingType {
    IMMEDIATE("즉시 송금"), PENDING("지연 송금");

    private final String description;
}
