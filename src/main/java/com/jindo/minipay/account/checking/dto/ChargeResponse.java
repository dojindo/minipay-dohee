package com.jindo.minipay.account.checking.dto;

import com.jindo.minipay.account.checking.entity.CheckingAccount;

public record ChargeResponse(
        String accountNumber,
        Long balance
) {
    public static ChargeResponse fromEntity(CheckingAccount account) {
        return new ChargeResponse(account.getAccountNumber(),
                account.getBalance());
    }
}
