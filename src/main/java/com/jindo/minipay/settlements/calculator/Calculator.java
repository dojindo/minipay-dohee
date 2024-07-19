package com.jindo.minipay.settlements.calculator;

import java.util.Collections;
import java.util.List;

public interface Calculator {
    List<Long> calculateAmount(int numOfParticipants, long totalAmount);

    default void fillZeroAndShuffle(int numOfParticipants, List<Long> requestAmounts) {
        while (requestAmounts.size() < numOfParticipants) {
            requestAmounts.add(0L);
        }
        Collections.shuffle(requestAmounts);
    }
}
