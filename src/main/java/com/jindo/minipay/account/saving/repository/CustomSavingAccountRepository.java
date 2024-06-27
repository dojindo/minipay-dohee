package com.jindo.minipay.account.saving.repository;

import com.jindo.minipay.account.saving.entity.SavingAccount;

import java.util.Optional;

public interface CustomSavingAccountRepository {
    Optional<SavingAccount> findByAccountNumberFetchJoin(String accountNumber);
}
