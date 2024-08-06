package com.jindo.minipay.lock.service.impl;

import com.jindo.minipay.lock.exception.LockException;
import com.jindo.minipay.lock.service.LockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.jindo.minipay.global.exception.ErrorCode.INTERNAL_ERROR;
import static com.jindo.minipay.global.exception.ErrorCode.RESOURCE_LOCKED;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedissonLockService implements LockService {
    private final RedissonClient redissonClient;
    private static final String KEY_PREFIX = "RSLK:";

    @Override
    public void getLock(String key, Long waitTime, Long releaseTime) {
        RLock lock = redissonClient.getLock(getKey(key));

        try {
            log.info("try lock (key: {})", key);
            boolean isLocked = lock.tryLock(waitTime, releaseTime, TimeUnit.SECONDS);

            if (!isLocked) {
                log.error("lock acquisition failed (key: {})", key);
                throw new LockException(RESOURCE_LOCKED);
            }
        } catch (InterruptedException e) {
            throw new LockException(INTERNAL_ERROR, e.getMessage());
        }
    }

    @Override
    public void releaseLock(String key) {
        RLock lock = redissonClient.getLock(getKey(key));

        if (lock.isLocked()) {
            log.info("release lock (key: {})", key);
            lock.unlock();
        }
    }

    private String getKey(String key) {
        return KEY_PREFIX + key;
    }
}
