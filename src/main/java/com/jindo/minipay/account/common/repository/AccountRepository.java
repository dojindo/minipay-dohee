package com.jindo.minipay.account.common.repository;

public interface AccountRepository {
    boolean existsByAccountNumber(String accountNumber);
}
