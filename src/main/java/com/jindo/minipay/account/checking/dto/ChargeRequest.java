package com.jindo.minipay.account.checking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChargeRequest(
        @NotBlank
        String accountNumber,

        @NotNull @Min(1)
        Long amount
) {
}
