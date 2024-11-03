package com.jindo.minipay.transaction.service;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.transaction.dto.RemitRequest;
import com.jindo.minipay.transaction.dto.RemitResponse;
import com.jindo.minipay.transaction.exception.TransactionException;
import com.jindo.minipay.transaction.repository.TransactionRepository;
import com.jindo.minipay.transaction.service.remit.RemitService;
import com.jindo.minipay.transaction.type.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_ACCOUNT_NUMBER;

@RequiredArgsConstructor
@Service
public class TransactionService {
    private final RemitService remitService;
    private final TransactionRepository transactionRepository;
    private final CheckingAccountRepository checkingAccountRepository;

    public RemitResponse remit(RemitRequest request) {
        return remitService.remit(request);
    }

    public void saveRemitFailed(RemitRequest request) {
        CheckingAccount senderAccount = findAccountOrThrow(request.senderAccountNumber());
        CheckingAccount receiverAccount = findAccountOrThrow(request.receiverAccountNumber());

        transactionRepository.save(request.toEntity(senderAccount, receiverAccount,
                TransactionStatus.FAILED));
    }

    private CheckingAccount findAccountOrThrow(String accountNumber) {
        return checkingAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new TransactionException(NOT_FOUND_ACCOUNT_NUMBER));
    }
}
