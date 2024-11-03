package com.jindo.minipay.transaction.service.remit.strategy;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.checking.service.charge.ChargeService;
import com.jindo.minipay.transaction.dto.RemitRequest;
import com.jindo.minipay.transaction.exception.TransactionException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;

import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_ACCOUNT_NUMBER;

@RequiredArgsConstructor
public abstract class RemitStrategy {
    private final CheckingAccountRepository checkingAccountRepository;
    private final ChargeService chargeService;

    public abstract Pair<CheckingAccount, CheckingAccount> remit(
            RemitRequest request, String email);

    public final CheckingAccount checkSenderAccount(String senderAccountNumber,
                                                    Long amount,
                                                    String email) {
        CheckingAccount senderCheckingAccount =
                getCheckingAccountOrThrow(senderAccountNumber);

        chargeService.autoChargingOrNot(senderCheckingAccount, email, amount);

        senderCheckingAccount.decreaseBalance(amount);
        return senderCheckingAccount;
    }

    public final CheckingAccount getCheckingAccountOrThrow(String accountNumber) {
        return checkingAccountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new TransactionException(NOT_FOUND_ACCOUNT_NUMBER));
    }
}
