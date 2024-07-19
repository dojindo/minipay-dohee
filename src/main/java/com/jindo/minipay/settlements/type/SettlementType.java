package com.jindo.minipay.settlements.type;

public enum SettlementType {
    DUTCH_PAY, RANDOM;

    public static SettlementType of(String value) {
        return SettlementType.valueOf(value.toUpperCase());
    }
}
