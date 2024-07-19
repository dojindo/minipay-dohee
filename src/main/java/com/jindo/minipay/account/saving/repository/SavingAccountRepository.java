package com.jindo.minipay.account.saving.repository;

import com.jindo.minipay.account.common.repository.AccountRepository;
import com.jindo.minipay.account.saving.entity.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long>,
        AccountRepository {
    boolean existsByAccountNumber(String accountNumber);

    Optional<SavingAccount> findByAccountNumber(String accountNumber);
}
