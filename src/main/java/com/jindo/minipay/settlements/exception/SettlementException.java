package com.jindo.minipay.settlements.exception;

import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.global.exception.ErrorCode;

public class SettlementException extends CustomException {
    public SettlementException(ErrorCode errorCode) {
        super(errorCode);
    }
}
