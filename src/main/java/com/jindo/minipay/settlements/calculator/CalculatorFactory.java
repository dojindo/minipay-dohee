package com.jindo.minipay.settlements.calculator;

import com.jindo.minipay.settlements.calculator.impl.DutchPayCalculator;
import com.jindo.minipay.settlements.calculator.impl.RandomCalculator;
import com.jindo.minipay.settlements.exception.SettlementException;
import com.jindo.minipay.settlements.type.SettlementType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.jindo.minipay.global.exception.ErrorCode.INVALID_SETTLEMENT_TYPE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CalculatorFactory {
    private static final DutchPayCalculator dutchPayCalculator = new DutchPayCalculator();
    private static final RandomCalculator randomCalculator = new RandomCalculator();

    public static Calculator of(SettlementType type) {
        switch (type) {
            case DUTCH_PAY -> {
                return dutchPayCalculator;
            }
            case RANDOM -> {
                return randomCalculator;
            }
            default -> throw new SettlementException(INVALID_SETTLEMENT_TYPE);
        }
    }
}
