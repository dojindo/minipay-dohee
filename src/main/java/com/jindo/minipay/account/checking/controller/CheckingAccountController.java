package com.jindo.minipay.account.checking.controller;

import com.jindo.minipay.account.checking.dto.ChargeRequest;
import com.jindo.minipay.account.checking.dto.ChargeResponse;
import com.jindo.minipay.account.checking.dto.RemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import com.jindo.minipay.account.checking.service.CheckingAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/account/checking")
@RequiredArgsConstructor
@RestController
public class CheckingAccountController {
    private final CheckingAccountService checkingAccountService;

    /**
     * 충전하기
     * @param request 계좌번호, 충전 금액
     * @return 계좌번호, 남은 금액
     */
    @PostMapping("/charge")
    public ResponseEntity<ChargeResponse> charge(
            @RequestBody @Valid ChargeRequest request) {
        return ResponseEntity.ok(checkingAccountService.charge(request));
    }

    /**
     * 송금하기 (자동 충전)
     * @param request 내 계좌, 수신 계좌, 금액
     * @return 내 계좌, 잔액
     */
    @PostMapping("/remit")
    public ResponseEntity<RemitResponse> remit(
            @RequestBody @Valid RemitRequest request) {
        return ResponseEntity.ok(checkingAccountService.remit(request));
    }
}
