package com.jindo.minipay.account.checking.repository;

import com.jindo.minipay.account.checking.entity.CheckingAccount;

import java.util.Optional;

public interface CustomCheckingAccountRepository {
    Optional<CheckingAccount> findByAccountNumberFetchJoin(String accountNumber);
}
