package com.jindo.minipay.account.checking.repository;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.common.repository.AccountRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckingAccountRepository extends JpaRepository<CheckingAccount, Long>,
        CustomCheckingAccountRepository, AccountRepository {
    boolean existsByAccountNumber(String accountNumber);
}
