package com.jindo.minipay.settlements.calculator.impl;

import com.jindo.minipay.settlements.calculator.Calculator;
import com.jindo.minipay.settlements.calculator.CalculatorFactory;
import com.jindo.minipay.settlements.type.SettlementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DutchPayCalculatorTest {
    static Calculator calculator = CalculatorFactory.of(SettlementType.DUTCH_PAY);

    @Test
    @DisplayName("더치 페이 금액을 계산한다.")
    void calculateAmount() {
        // given
        // when
        List<Long> requestAmounts =
                calculator.calculateAmount(3, 30000L);

        // then
        Long sumAmounts = requestAmounts.stream()
                .mapToLong(o -> o)
                .sum();

        assertEquals(10000, requestAmounts.get(0));
        assertEquals(30000L, sumAmounts);
    }

    @Test
    @DisplayName("더치 페이 금액 계산 시 나누어 떨어지지 않는 경우 남는 금액은 결과 값에 포함하지 않는다.")
    void calculateAmount_remainingAmount() {
        // given
        // when
        List<Long> requestAmounts =
                calculator.calculateAmount(3, 10000L);

        // then
        Long sumAmounts = requestAmounts.stream()
                .mapToLong(o -> o)
                .sum();

        assertEquals(3333L, requestAmounts.get(0));
        assertEquals(10000L - 1, sumAmounts);
    }

    @Test
    @DisplayName("총 금액이 정산 인원 수 보다 작은 경우 한명이 모두 부담한다.")
    void calculateAmount_totalAmount_is_smaller() {
        // given
        // when
        List<Long> requestAmounts =
                calculator.calculateAmount(11, 10L);

        // then
        Long sumAmounts = requestAmounts.stream()
                .mapToLong(o -> o)
                .sum();

        long count = requestAmounts.stream()
                .filter(amount -> amount > 0)
                .count();

        assertEquals(10, sumAmounts);
        assertEquals(1, count);
    }
}