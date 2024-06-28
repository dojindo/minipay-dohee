package com.jindo.minipay.lock.service;

public interface LockService {
    void getLock(String key, Long waitTime, Long releaseTime) throws InterruptedException;

    void releaseLock(String key);
}
