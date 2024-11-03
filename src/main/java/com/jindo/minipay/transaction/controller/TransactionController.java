package com.jindo.minipay.transaction.controller;

import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.transaction.dto.RemitRequest;
import com.jindo.minipay.transaction.dto.RemitResponse;
import com.jindo.minipay.transaction.exception.TransactionException;
import com.jindo.minipay.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@RestController
public class TransactionController {
    private final TransactionService transactionService;

    /**
     * 송금하기 (자동 충전)
     * @param request 내 계좌, 수신 계좌, 금액
     * @return 내 계좌, 잔액
     */
    @PostMapping("/remit")
    public ResponseEntity<RemitResponse> remit(
            @RequestBody @Valid RemitRequest request) {
        try {
            return ResponseEntity.ok(transactionService.remit(request));
        } catch (TransactionException | AccountException e) {
            transactionService.saveRemitFailed(request);
            throw e;
        }
    }
}
