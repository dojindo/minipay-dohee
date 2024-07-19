package com.jindo.minipay.settlements.repository;

import com.jindo.minipay.settlements.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
}
