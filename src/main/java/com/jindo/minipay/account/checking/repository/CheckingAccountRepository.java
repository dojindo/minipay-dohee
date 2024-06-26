package com.jindo.minipay.account.checking.repository;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckingAccountRepository extends JpaRepository<CheckingAccount, Long> {
    boolean existsByAccountNumber(String accountNumber);
}
