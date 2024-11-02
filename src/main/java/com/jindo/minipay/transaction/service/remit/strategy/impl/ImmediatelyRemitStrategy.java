package com.jindo.minipay.transaction.service.remit.strategy.impl;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.checking.service.charge.ChargeService;
import com.jindo.minipay.lock.annotation.DistributedMultiLock;
import com.jindo.minipay.transaction.dto.RemitRequest;
import com.jindo.minipay.transaction.service.remit.strategy.RemitStrategy;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ImmediatelyRemitStrategy extends RemitStrategy {
    public ImmediatelyRemitStrategy(CheckingAccountRepository checkingAccountRepository,
                                    ChargeService chargeService) {
        super(checkingAccountRepository, chargeService);
    }

    @DistributedMultiLock(keyFields = {"senderAccountNumber", "receiverAccountNumber"})
    @Transactional
    @Override
    public Pair<CheckingAccount, CheckingAccount> remit(RemitRequest request, String email) {
        CheckingAccount senderCheckingAccount =
                checkSenderAccount(request.senderAccountNumber(), request.amount(), email);

        CheckingAccount receiverCheckingAccount =
                sendMoneyToReceiver(request.receiverAccountNumber(), request.amount());

        return Pair.of(senderCheckingAccount, receiverCheckingAccount);
    }

    private CheckingAccount sendMoneyToReceiver(String receiverAccountNumber, Long amount) {
        CheckingAccount receiverCheckingAccount =
                getCheckingAccountOrThrow(receiverAccountNumber);

        receiverCheckingAccount.increaseBalance(amount);
        return receiverCheckingAccount;
    }
}
