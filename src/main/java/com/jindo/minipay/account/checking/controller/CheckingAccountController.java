package com.jindo.minipay.account.checking.controller;

import com.jindo.minipay.account.checking.dto.ChargeRequest;
import com.jindo.minipay.account.checking.dto.ChargeResponse;
import com.jindo.minipay.account.checking.service.CheckingAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/account/checking")
@RequiredArgsConstructor
@RestController
public class CheckingAccountController {
    private final CheckingAccountService checkingAccountService;

    @PostMapping("/charge")
    public ResponseEntity<ChargeResponse> charge(
            @RequestBody @Valid ChargeRequest request) {
        return ResponseEntity.ok(checkingAccountService.charge(request));
    }
}
