package com.jindo.minipay.account.saving.repository;

import com.jindo.minipay.account.saving.entity.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long> {
}
