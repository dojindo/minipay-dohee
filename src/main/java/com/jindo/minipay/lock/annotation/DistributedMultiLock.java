package com.jindo.minipay.lock.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD })
@Retention(RUNTIME)
public @interface DistributedMultiLock {
    String[] keyFields();

    long waitTime() default 3L;

    long releaseTime() default 5L;
}
