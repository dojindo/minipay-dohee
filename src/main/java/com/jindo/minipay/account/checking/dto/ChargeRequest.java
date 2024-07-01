package com.jindo.minipay.account.checking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ChargeRequest(
        @NotBlank
        String accountNumber,

        @NotNull
        @PositiveOrZero
        Long amount
) {
}
