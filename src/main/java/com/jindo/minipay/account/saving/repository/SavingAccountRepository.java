package com.jindo.minipay.account.saving.repository;

import com.jindo.minipay.account.common.repository.AccountRepository;
import com.jindo.minipay.account.saving.entity.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long>,
        CustomSavingAccountRepository, AccountRepository {
    boolean existsByAccountNumber(String accountNumber);
}
