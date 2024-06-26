package com.jindo.minipay.account.saving.controller;

import com.jindo.minipay.account.saving.dto.CreateSavingAccountRequest;
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

    @PostMapping
    public ResponseEntity<Void> create(
            @RequestBody @Valid CreateSavingAccountRequest request) {
        Long accountId = savingAccountService.createAccount(request);
        return ResponseEntity
                .created(URI.create("/api/v1/account/saving/" + accountId))
                .build();
    }
}
