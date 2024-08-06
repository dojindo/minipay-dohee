package com.jindo.minipay.lock.aop;

import com.jindo.minipay.lock.service.LockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CustomTransactionSynchronization  implements TransactionSynchronization {
    private final LockService lockService;
    private final String key;
    private final List<String> keys;

    private CustomTransactionSynchronization(LockService lockService, String key) {
        this.lockService = lockService;
        this.key = key;
        this.keys = new ArrayList<>();
    }

    private CustomTransactionSynchronization(LockService lockService, List<String> keys) {
        this.lockService = lockService;
        this.keys = keys;
        this.key = null;
    }

    public static CustomTransactionSynchronization of(LockService lockService, String key) {
        return new CustomTransactionSynchronization(lockService, key);
    }

    public static CustomTransactionSynchronization ofMultiKey(LockService lockService,
                                                              List<String> keys) {
        return new CustomTransactionSynchronization(lockService, keys);
    }

    @Override
    public void afterCompletion(int status) {
        if (key != null) {
            lockService.releaseLock(key);
        } else {
            keys.forEach(lockService::releaseLock);
        }
    }
}
