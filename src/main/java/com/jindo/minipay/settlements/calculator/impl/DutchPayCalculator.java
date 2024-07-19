package com.jindo.minipay.settlements.calculator.impl;

import com.jindo.minipay.settlements.calculator.Calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class DutchPayCalculator implements Calculator {
    @Override
    public List<Long> calculateAmount(int numOfParticipants, long totalAmount) {
        List<Long> requestAmounts = new ArrayList<>();

        if (totalAmount < numOfParticipants) {
            requestAmounts.add(totalAmount);
            fillZeroAndShuffle(numOfParticipants, requestAmounts);
        } else {
            long requestAmount = totalAmount / numOfParticipants;
            requestAmounts = Stream.generate(() -> requestAmount)
                    .limit(numOfParticipants)
                    .toList();
        }

        return requestAmounts;
    }
}
