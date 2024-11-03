package com.jindo.minipay.transaction.repository;

import com.jindo.minipay.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
