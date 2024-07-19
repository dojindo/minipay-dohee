package com.jindo.minipay.settlements.repository;

import com.jindo.minipay.settlements.entity.SettlementParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementParticipantRepository extends JpaRepository<SettlementParticipant, Long> {
}
