package com.jindo.minipay.settlements.controller;

import com.jindo.minipay.settlements.dto.SettleAccountsRequest;
import com.jindo.minipay.settlements.dto.SettleAccountsResponse;
import com.jindo.minipay.settlements.dto.SettleCalculateRequest;
import com.jindo.minipay.settlements.dto.SettleCalculateResponse;
import com.jindo.minipay.settlements.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
@RestController
public class SettlementController {
    private final SettlementService settlementService;

    /**
     * 정산 금액 계산
     * @param request 정산 정보
     * @return 정산 금액 계산 결과
     */
    @PostMapping("/calculate")
    public ResponseEntity<SettleCalculateResponse> settleCalculate(
            @RequestBody @Valid SettleCalculateRequest request) {
        return ResponseEntity.ok(settlementService.settleCalculate(request));
    }

    /**
     * 정산 요청
     * @param request 정산 정보
     * @return 정산 정보 저장 결과
     */
    @PostMapping
    public ResponseEntity<SettleAccountsResponse> settleAccounts(
            @RequestBody @Valid SettleAccountsRequest request) {
        return ResponseEntity.ok(settlementService.settleAccounts(request));
    }
}
