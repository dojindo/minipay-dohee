package com.jindo.minipay.fcm.dto;

public record NotificationDto(
        Long memberId,
        String title,
        String body
) {
}
