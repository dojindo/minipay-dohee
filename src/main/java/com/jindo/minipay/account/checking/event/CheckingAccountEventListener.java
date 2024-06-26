package com.jindo.minipay.account.checking.event;

import com.jindo.minipay.account.checking.service.CheckingAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class CheckingAccountEventListener {
    private final CheckingAccountService checkingAccountService;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCreateCheckingAccount(CreateCheckingAccountEvent event) {
        checkingAccountService.createCheckingAccount(event.memberId());
    }
}
