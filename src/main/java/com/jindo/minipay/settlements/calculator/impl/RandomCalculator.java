package com.jindo.minipay.settlements.calculator.impl;

import com.jindo.minipay.settlements.calculator.Calculator;

import java.security.SecureRandom;
import java.util.*;

public class RandomCalculator implements Calculator {
    private static final Random RANDOM = new SecureRandom();
    private static final int UNIT = 100;
    private static final int MIN_TOTAL_AMOUNT = 500;

    @Override
    public List<Long> calculateAmount(int numOfParticipants, long totalAmount) {
        List<Long> requestAmounts = new ArrayList<>();

        if (totalAmount < MIN_TOTAL_AMOUNT || totalAmount < numOfParticipants) {
            requestAmounts.add(totalAmount);
        } else {
            long amount = totalAmount;
            long sumAmount = 0;

            for (int i = 0; i < numOfParticipants - 1; i++) {
                long num = RANDOM.nextLong(UNIT, amount);
                amount = Math.min(num, amount - num);
                amount = getAmount(amount);

                sumAmount += amount;
                requestAmounts.add(amount);

                if (amount <= UNIT) {
                    break;
                }
            }

            requestAmounts.add(totalAmount - sumAmount);
        }

        fillZeroAndShuffle(numOfParticipants, requestAmounts);
        return requestAmounts;
    }

    private static long getAmount(long amount) { // 100 단위 절삭
        return amount > UNIT ? amount / UNIT * UNIT : amount;
    }
}
