package com.jindo.minipay.transaction.exception;

import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.global.exception.ErrorCode;

public class TransactionException extends CustomException {
    public TransactionException(ErrorCode errorCode) {
        super(errorCode);
    }
}
