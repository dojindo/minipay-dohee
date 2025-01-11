package com.jindo.minipay.fcm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.jindo.minipay.fcm.entity.FcmToken;
import com.jindo.minipay.fcm.exception.FcmException;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.fcm.dto.NotificationDto;
import com.jindo.minipay.fcm.dto.TokenRegistrationRequest;
import com.jindo.minipay.fcm.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static com.jindo.minipay.global.exception.ErrorCode.FCM_SEND_MESSAGE_EXCEPTION;
import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_FCM_TOKEN;
import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_MEMBER;

@Slf4j
@RequiredArgsConstructor
@Service
public class FcmService {
    private final FcmTokenRepository fcmTokenRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void tokenRegistration(TokenRegistrationRequest request) {
        Member member = getMember(request.memberId());

        fcmTokenRepository.findByMember(member)
                .ifPresentOrElse(o -> o.updateToken(request.fcmToken()),
                        () -> fcmTokenRepository.save(request.toEntity(member)));
    }

    public void sendNotification(NotificationDto notificationDto) {
        Member member = getMember(notificationDto.memberId());

        FcmToken fcmToken = fcmTokenRepository.findByMember(member)
                .orElseThrow(() -> new FcmException(NOT_FOUND_FCM_TOKEN));

        sendNotificationToDevice(fcmToken.getToken(), notificationDto.title(), notificationDto.body());
    }

    private void sendNotificationToDevice(String token, String title, String body) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.error("FirebaseMessagingException is occurred. ", e);
            throw new FcmException(FCM_SEND_MESSAGE_EXCEPTION,
                    StringUtils.hasText(e.getMessage()) ? e.getMessage() : FCM_SEND_MESSAGE_EXCEPTION.getMessage());
        }
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new FcmException(NOT_FOUND_MEMBER));
    }
}
