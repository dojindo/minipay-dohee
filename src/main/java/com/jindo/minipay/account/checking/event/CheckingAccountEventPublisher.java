package com.jindo.minipay.account.checking.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CheckingAccountEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public void publishCreateCheckingAccount(CreateCheckingAccountEvent event) {
        eventPublisher.publishEvent(event);
    }
}
