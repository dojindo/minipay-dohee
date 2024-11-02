package com.jindo.minipay.transaction.service.remit.strategy.impl;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.checking.service.charge.ChargeService;
import com.jindo.minipay.lock.annotation.DistributedLock;
import com.jindo.minipay.transaction.dto.RemitRequest;
import com.jindo.minipay.transaction.service.remit.strategy.RemitStrategy;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PendingRemitStrategy extends RemitStrategy {
    public PendingRemitStrategy(CheckingAccountRepository checkingAccountRepository,
                                ChargeService chargeService) {
        super(checkingAccountRepository, chargeService);
    }

    @DistributedLock(keyField = "senderAccountNumber")
    @Transactional
    @Override
    public Pair<CheckingAccount, CheckingAccount> remit(RemitRequest request, String email) {
        CheckingAccount senderCheckingAccount =
                checkSenderAccount(request.senderAccountNumber(), request.amount(), email);

        CheckingAccount receiverCheckingAccount =
                getCheckingAccountOrThrow(request.receiverAccountNumber());

        return Pair.of(senderCheckingAccount, receiverCheckingAccount);
    }
}
