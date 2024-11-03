package com.jindo.minipay.transaction.dto;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.transaction.entity.Transaction;
import com.jindo.minipay.transaction.type.TransactionStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RemitRequest(
        @NotBlank
        String senderAccountNumber,

        @NotBlank
        String receiverAccountNumber,

        @NotNull @Min(1)
        Long amount
) {
    public Transaction toEntity(CheckingAccount sender, CheckingAccount receiver,
                                TransactionStatus status) {
        return Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(amount)
                .status(status)
                .build();
    }
}
