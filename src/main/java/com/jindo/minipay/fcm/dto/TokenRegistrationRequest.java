package com.jindo.minipay.fcm.dto;

import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.fcm.entity.FcmToken;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TokenRegistrationRequest(
        @NotNull
        Long memberId,

        @NotBlank
        String fcmToken
) {
    public FcmToken toEntity(Member member) {
        return FcmToken.builder()
                .token(fcmToken)
                .member(member)
                .build();
    }
}
