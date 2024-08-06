package com.jindo.minipay.lock.aop;

import com.jindo.minipay.lock.annotation.DistributedLock;
import com.jindo.minipay.lock.annotation.DistributedMultiLock;
import com.jindo.minipay.lock.service.LockService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Aspect
@Component
public class DistributedLockAspect {
    private final LockService lockService;

    @Around("@annotation(com.jindo.minipay.lock.annotation.DistributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        DistributedLock annotation = getAnnotation(joinPoint, DistributedLock.class);

        String key = parseSingleKey(joinPoint, annotation);
        lockService.getLock(key, annotation.waitTime(), annotation.releaseTime());

        TransactionSynchronizationManager.registerSynchronization(
                CustomTransactionSynchronization.of(lockService, key));

        return joinPoint.proceed();
    }

    @Around("@annotation(com.jindo.minipay.lock.annotation.DistributedMultiLock)")
    public Object multiLock(ProceedingJoinPoint joinPoint) throws Throwable {
        DistributedMultiLock annotation =
                getAnnotation(joinPoint, DistributedMultiLock.class);

        List<String> keys = parseMultiKey(joinPoint, annotation);
        keys.sort(Comparator.naturalOrder());

        for (String key : keys) {
            lockService.getLock(key, annotation.waitTime(), annotation.releaseTime());
        }

        TransactionSynchronizationManager.registerSynchronization(
                CustomTransactionSynchronization.ofMultiKey(lockService, keys));

        return joinPoint.proceed();
    }

    private List<String> parseMultiKey(ProceedingJoinPoint joinPoint,
                                       DistributedMultiLock annotation)
            throws IllegalAccessException {
        List<String> keys = new ArrayList<>();

        for (Object obj : joinPoint.getArgs()) {
            Field[] fields = obj.getClass().getDeclaredFields();

            for (String key : annotation.keyFields()) {
                for (Field field : fields) {
                    if (field.getName().equals(key)) {
                        field.trySetAccessible();
                        keys.add(field.get(obj).toString());
                        break;
                    }
                }
            }
        }

        return keys;
    }

    private String parseSingleKey(ProceedingJoinPoint joinPoint,
                                  DistributedLock annotation)
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

    private <A extends Annotation> A getAnnotation(
            ProceedingJoinPoint joinPoint, Class<A> aClass) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(aClass);
    }
}
