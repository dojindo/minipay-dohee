package com.jindo.minipay.fcm.exception;

import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.global.exception.ErrorCode;

public class FcmException extends CustomException {
    public FcmException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FcmException(ErrorCode errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
