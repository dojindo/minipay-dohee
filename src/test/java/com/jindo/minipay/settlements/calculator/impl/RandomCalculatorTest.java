package com.jindo.minipay.settlements.calculator.impl;

import com.jindo.minipay.settlements.calculator.Calculator;
import com.jindo.minipay.settlements.calculator.CalculatorFactory;
import com.jindo.minipay.settlements.type.SettlementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RandomCalculatorTest {
    static Calculator calculator = CalculatorFactory.of(SettlementType.RANDOM);

    @Test
    @DisplayName("랜덤 정산 금액을 계산한다.")
    void calculateAmount() {
        // given
        // when
        List<Long> requestAmounts =
                calculator.calculateAmount(2, 30000L);

        // then
        Long sumAmounts = requestAmounts.stream()
                .mapToLong(o -> o)
                .sum();

        assertNotEquals(requestAmounts.get(0), requestAmounts.get(1));
        assertEquals(30000L, sumAmounts);
    }

    static Stream<Arguments> provideNumOfParticipantsAndTotalAmount() {
        return Stream.of(
                Arguments.of(400, 2),
                Arguments.of(11, 10)
        );
    }

    @ParameterizedTest
    @MethodSource("provideNumOfParticipantsAndTotalAmount")
    @DisplayName("총 금액이 최소 금액보다 작거나 정산 인원 수 보다 작은 경우 한명이 모두 부담한다.")
    void calculateAmount_totalAmount_is_smaller(int numOfParticipants, long totalAmount) {
        // given
        // when
        List<Long> requestAmounts =
                calculator.calculateAmount(numOfParticipants, totalAmount);

        // then
        Long sumAmounts = requestAmounts.stream()
                .mapToLong(o -> o)
                .sum();

        long count = requestAmounts.stream()
                .filter(amount -> amount > 0)
                .count();

        assertEquals(totalAmount, sumAmounts);
        assertEquals(1, count);
    }
}