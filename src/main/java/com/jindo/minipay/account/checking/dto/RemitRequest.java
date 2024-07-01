package com.jindo.minipay.account.checking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RemitRequest(
        @NotBlank
        String myAccountNumber,

        @NotBlank
        String receiverAccountNumber,

        @NotNull @Min(1)
        Long amount
) {
}
