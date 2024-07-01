package com.jindo.minipay.lock.aop;

import com.jindo.minipay.lock.annotation.DistributedLock;
import com.jindo.minipay.lock.service.LockService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@RequiredArgsConstructor
@Aspect
@Component
public class DistributedLockAspect {
    private final LockService lockService;

    @Around("@annotation(com.jindo.minipay.lock.annotation.DistributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock annotation = method.getAnnotation(DistributedLock.class);

        String key = parseKey(joinPoint, annotation);
        lockService.getLock(key, annotation.waitTime(), annotation.releaseTime());

        TransactionSynchronizationManager.registerSynchronization(
                new CustomTransactionSynchronization(lockService, key));

        return joinPoint.proceed();
    }

    private String parseKey(ProceedingJoinPoint joinPoint, DistributedLock annotation)
            throws IllegalAccessException {
        for (Object obj : joinPoint.getArgs()) {
            Field[] fields = obj.getClass().getDeclaredFields();

            for (Field field : fields) {
                if (field.getName().equals(annotation.keyField())) {
                    field.trySetAccessible();
                    return field.get(obj).toString();
                }
            }
        }
        return null;
    }
}
