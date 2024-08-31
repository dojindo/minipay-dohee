package com.jindo.minipay.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

import static com.jindo.minipay.global.exception.ErrorCode.INTERNAL_ERROR;
import static com.jindo.minipay.global.exception.ErrorCode.INVALID_REQUEST;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String ARG_VALID_MSG = "%s 필드의(는) %s (전달된 값: %s)";

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        return ErrorResponse.of(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse.ErrorResponseArray> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getFieldErrors();
        String[] errorMessages = new String[fieldErrors.size()];

        for (int i = 0; i < fieldErrors.size(); i++) {
            FieldError fieldError = fieldErrors.get(i);
            String errorMessage = String.format(ARG_VALID_MSG,
                    fieldError.getField(),
                    fieldError.getDefaultMessage(),
                    fieldError.getRejectedValue());
            errorMessages[i] = errorMessage;
        }

        return ErrorResponse.ofArray(INVALID_REQUEST, errorMessages);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("Exception is occurred. ", e);
        return ErrorResponse.of(INTERNAL_ERROR, e.getMessage());
    }
}
