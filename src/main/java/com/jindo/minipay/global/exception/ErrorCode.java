package com.jindo.minipay.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // global
    INVALID_REQUEST(BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_ERROR(INTERNAL_SERVER_ERROR, "예상치 못한 내부 문제가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
