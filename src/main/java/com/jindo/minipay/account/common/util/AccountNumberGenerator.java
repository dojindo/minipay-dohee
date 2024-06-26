package com.jindo.minipay.account.common.util;


import com.jindo.minipay.account.common.type.AccountType;

import java.util.Random;

public final class AccountNumberGenerator {
    private static final Random RANDOM = new Random();
    private static final int MID_LENGTH = 2;
    private static final int END_LENGTH = 7;

    // 일련번호 무작위 추출 8888-01-1234567
    public static String generateAccountNumber(AccountType accountType) {
        StringBuilder sb = new StringBuilder();
        sb.append(accountType.getCode()).append("-");

        for (int i = 0; i < MID_LENGTH; i++) {
            sb.append(getNextInt());
        }

        sb.append("-");

        for (int i = 0; i < END_LENGTH; i++) {
            sb.append(getNextInt());
        }

        return sb.toString();
    }

    private static int getNextInt() {
        return RANDOM.nextInt(10);
    }
}
