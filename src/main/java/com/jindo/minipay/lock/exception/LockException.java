package com.jindo.minipay.lock.exception;

import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.global.exception.ErrorCode;

public class LockException extends CustomException {
    public LockException(ErrorCode errorCode) {
        super(errorCode);
    }

    public LockException(ErrorCode errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
