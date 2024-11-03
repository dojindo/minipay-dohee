package com.jindo.minipay.setting.exception;

import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.global.exception.ErrorCode;

public class SettingException extends CustomException {
    public SettingException(ErrorCode errorCode) {
        super(errorCode);
    }
}
