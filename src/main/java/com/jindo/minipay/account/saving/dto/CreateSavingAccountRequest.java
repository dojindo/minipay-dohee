package com.jindo.minipay.account.saving.dto;

import jakarta.validation.constraints.NotNull;

public record CreateSavingAccountRequest(
        @NotNull
        Long memberId
) {
}
