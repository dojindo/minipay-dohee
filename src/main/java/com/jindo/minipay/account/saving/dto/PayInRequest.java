package com.jindo.minipay.account.saving.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

@Builder
public record PayInRequest(
        @NotBlank
        String savingAccountNumber,

        @NotNull
        String checkingAccountNumber,

        @NotNull
        @PositiveOrZero
        Long amount
) {
}
