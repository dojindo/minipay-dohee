package com.jindo.minipay.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.jindo.minipay.global.exception.ErrorCode.INTERNAL_ERROR;
import static com.jindo.minipay.global.exception.ErrorCode.INVALID_REQUEST;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        return ErrorResponse.of(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        FieldError fieldError = e.getFieldErrors().get(0);
        String errorMessage = String.format("%s 필드의(는) %s (전달된 값: %s)",
                fieldError.getField(),
                fieldError.getDefaultMessage(),
                fieldError.getRejectedValue());
        return ErrorResponse.of(INVALID_REQUEST, errorMessage);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("Exception is occurred. ", e);
        return ErrorResponse.of(INTERNAL_ERROR, e.getMessage());
    }
}
