package com.jindo.minipay.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // member
    ALREADY_EXISTS_MEMBER(BAD_REQUEST, "이미 가입된 회원입니다."),
    NOT_FOUND_MEMBER(NOT_FOUND, "존재하지 않는 회원입니다."),

    // account
    NOT_FOUND_ACCOUNT_NUMBER(NOT_FOUND, "존재하지 않는 계좌번호입니다."),
    EXCEEDED_DAILY_CHARGING_LIMIT(BAD_REQUEST, "1일 충전 한도를 초과했습니다."),
    INSUFFICIENT_BALANCE(BAD_REQUEST, "잔액이 부족합니다."),

    // settlement
    NOT_FOUND_SETTLEMENT_PARTICIPANTS(NOT_FOUND, "존재하지 않는 정산 참여자가 있습니다."),
    INVALID_SETTLEMENT_TYPE(BAD_REQUEST, "잘못된 정산 타입입니다."),
    INCORRECT_TOTAL_AMOUNT(BAD_REQUEST, "총 금액이 맞지 않습니다."),

    // global
    INVALID_REQUEST(BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_ERROR(INTERNAL_SERVER_ERROR, "예상치 못한 내부 문제가 발생했습니다."),
    RESOURCE_LOCKED(LOCKED, "자원이 잠겨있어 접근할 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
