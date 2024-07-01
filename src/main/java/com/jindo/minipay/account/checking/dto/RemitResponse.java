package com.jindo.minipay.account.checking.dto;

import com.jindo.minipay.account.checking.entity.CheckingAccount;

public record RemitResponse(
        String accountNumber,
        Long balance
) {
    public static RemitResponse fromEntity(CheckingAccount checkingAccount) {
        return new RemitResponse(checkingAccount.getAccountNumber(),
                checkingAccount.getBalance());
    }
}
