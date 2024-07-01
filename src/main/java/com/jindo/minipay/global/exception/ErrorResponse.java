package com.jindo.minipay.global.exception;

import org.springframework.http.ResponseEntity;

public record ErrorResponse(
        ErrorCode errorCode,
        String errorMessage
) {
    public static ResponseEntity<ErrorResponse> of(ErrorCode errorCode, String errorMessage) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ErrorResponse(errorCode, errorMessage));
    }
}
