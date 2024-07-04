package com.jindo.minipay.account.checking.service;

import com.jindo.minipay.account.checking.dto.RemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RemitService {
    private final CheckingAccountService checkingAccountService;

    @Transactional
    public RemitResponse remit(RemitRequest request) {
        CheckingAccount myCheckingAccount =
                checkingAccountService.checkMyAccount(request);
        checkingAccountService.sendMoneyToReceiver(request);
        return RemitResponse.fromEntity(myCheckingAccount);
    }
}
