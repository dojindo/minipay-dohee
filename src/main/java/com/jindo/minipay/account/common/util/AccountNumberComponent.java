package com.jindo.minipay.account.common.util;

import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.repository.AccountRepository;
import com.jindo.minipay.account.common.type.AccountType;
import com.jindo.minipay.account.saving.repository.SavingAccountRepository;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Random;

import static com.jindo.minipay.account.common.type.AccountType.CHECKING;
import static com.jindo.minipay.account.common.type.AccountType.SAVINGS;

@Getter
@Component
public class AccountNumberComponent {
    private static final Random RANDOM = new Random();
    private static final int MID_LENGTH = 2;
    private static final int END_LENGTH = 7;

    private final EnumMap<AccountType, AccountRepository> repositoryEnumMap =
            new EnumMap<>(AccountType.class);

    public AccountNumberComponent(CheckingAccountRepository checkingAccountRepository,
                                  SavingAccountRepository savingAccountRepository) {
        repositoryEnumMap.put(CHECKING, checkingAccountRepository);
        repositoryEnumMap.put(SAVINGS, savingAccountRepository);
    }

    // TODO: 계좌가 많아질수록 중복 확인이 느려진다. 유저별로 계좌를 식별할 수 있으면 좋을듯
    public String getAccountNumber(AccountType accountType) {
        String accountNumber;
        boolean isNew;

        do {
            accountNumber = generateAccountNumber(accountType);
            isNew = !repositoryEnumMap.get(accountType)
                    .existsByAccountNumber(accountNumber);
        } while (!isNew);

        return accountNumber;
    }

    // 일련번호 무작위 추출 8888-01-1234567
    private String generateAccountNumber(AccountType accountType) {
        StringBuilder sb = new StringBuilder();
        sb.append(accountType.getCode()).append("-");

        sb.append(String.format("%02d",
                RANDOM.nextInt((int) Math.pow(10, MID_LENGTH))));

        sb.append("-");

        sb.append(String.format("%07d",
                RANDOM.nextInt((int) Math.pow(10, END_LENGTH))));

        return sb.toString();
    }
}
