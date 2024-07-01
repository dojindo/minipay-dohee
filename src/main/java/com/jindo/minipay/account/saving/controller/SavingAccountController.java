package com.jindo.minipay.account.saving.controller;

import com.jindo.minipay.account.saving.dto.CreateSavingAccountRequest;
import com.jindo.minipay.account.saving.dto.PayInRequest;
import com.jindo.minipay.account.saving.dto.PayInResponse;
import com.jindo.minipay.account.saving.service.SavingAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RequestMapping("/api/v1/account/saving")
@RequiredArgsConstructor
@RestController
public class SavingAccountController {
    private final SavingAccountService savingAccountService;

    /**
     * 적금 계좌 생성
     * @param request 회원 ID
     * @return void
     */
    @PostMapping
    public ResponseEntity<Void> create(
            @RequestBody @Valid CreateSavingAccountRequest request) {
        Long accountId = savingAccountService.createAccount(request);
        return ResponseEntity
                .created(URI.create("/api/v1/account/saving/" + accountId))
                .build();
    }

    /**
     * 적금 계좌 납입
     * @param request 계좌 번호, 금액
     * @return 납입 후 금액
     */
    @PostMapping("/payin")
    public ResponseEntity<PayInResponse> payIn(
            @RequestBody @Valid PayInRequest request) {
        return ResponseEntity.ok(savingAccountService.payIn(request));
    }
}
