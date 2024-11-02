package com.jindo.minipay.transaction.service;

import com.jindo.minipay.transaction.dto.RemitRequest;
import com.jindo.minipay.transaction.dto.RemitResponse;
import com.jindo.minipay.transaction.service.remit.RemitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TransactionService {
    private final RemitService remitService;

    public RemitResponse remit(RemitRequest request) {
        return remitService.remit(request);
    }
}
