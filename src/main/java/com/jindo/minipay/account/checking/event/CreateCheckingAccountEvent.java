package com.jindo.minipay.account.checking.event;

public record CreateCheckingAccountEvent(
        Long memberId
) {
    public static CreateCheckingAccountEvent of(Long memberId) {
        return new CreateCheckingAccountEvent(memberId);
    }
}
