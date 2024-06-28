package com.jindo.minipay.lock.aop;

import com.jindo.minipay.lock.service.LockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;

@Slf4j
public class CustomTransactionSynchronization  implements TransactionSynchronization {
    private final LockService lockService;
    private final String key;

    public CustomTransactionSynchronization(LockService lockService, String key) {
        this.lockService = lockService;
        this.key = key;
    }

    @Override
    public void afterCompletion(int status) {
        lockService.releaseLock(key);
    }
}
