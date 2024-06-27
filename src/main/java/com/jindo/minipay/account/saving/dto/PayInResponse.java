package com.jindo.minipay.account.saving.dto;

import com.jindo.minipay.account.saving.entity.SavingAccount;

public record PayInResponse(
        String accountNumber,
        Long amount
) {
    public static PayInResponse fromEntity(SavingAccount account) {
        return new PayInResponse(account.getAccountNumber(), account.getAmount());
    }
}
