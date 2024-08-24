package com.jindo.minipay.account.checking.event;

import com.jindo.minipay.account.checking.service.CheckingAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class CheckingAccountEventListener {
    private final CheckingAccountService checkingAccountService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCreateCheckingAccount(CreateCheckingAccountEvent event) {
        checkingAccountService.createAccount(event.memberId());
    }
}
