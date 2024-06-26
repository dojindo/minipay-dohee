package com.jindo.minipay.account.common.exception;

import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.global.exception.ErrorCode;

public class AccountException extends CustomException {
    public AccountException(ErrorCode errorCode) {
        super(errorCode);
    }
}
