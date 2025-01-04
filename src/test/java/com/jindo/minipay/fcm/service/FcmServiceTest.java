package com.jindo.minipay.fcm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.jindo.minipay.fcm.dto.NotificationDto;
import com.jindo.minipay.fcm.dto.TokenRegistrationRequest;
import com.jindo.minipay.fcm.entity.FcmToken;
import com.jindo.minipay.fcm.exception.FcmException;
import com.jindo.minipay.fcm.repository.FcmTokenRepository;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static com.jindo.minipay.global.exception.ErrorCode.FCM_SEND_MESSAGE_EXCEPTION;
import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FcmServiceTest {
    @Mock
    FcmTokenRepository fcmTokenRepository;

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    FcmService fcmService;

    @DisplayName("FCM token 등록 메서드")
    @Nested
    class TokenRegistrationMethod {
        Member member = Member.builder()
                .email("test@test.com")
                .password("test12345")
                .name("tester1")
                .build();

        TokenRegistrationRequest request = new TokenRegistrationRequest(1L, "newFcmToken");

        @DisplayName("FCM token을 신규 등록한다.")
        @Test
        void tokenRegistration() {
            // given
            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(member));

            given(fcmTokenRepository.findByMember(member))
                    .willReturn(Optional.empty());

            ArgumentCaptor<FcmToken> captor = ArgumentCaptor.forClass(FcmToken.class);

            // when
            fcmService.tokenRegistration(request);

            // then
            verify(fcmTokenRepository).save(captor.capture());
            FcmToken captorValue = captor.getValue();

            assertEquals(request.fcmToken(), captorValue.getToken());
            assertEquals(member, captorValue.getMember());
        }

        @DisplayName("FCM token을 업데이트한다.")
        @Test
        void tokenRegistration_update() {
            // given
            FcmToken fcmToken = FcmToken.builder()
                    .member(member)
                    .token("fcmToken")
                    .build();

            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(member));

            given(fcmTokenRepository.findByMember(member))
                    .willReturn(Optional.of(fcmToken));

            // when
            fcmService.tokenRegistration(request);

            // then
            verify(fcmTokenRepository, times(0)).save(any());
            assertEquals(request.fcmToken(), fcmToken.getToken());
        }

        @DisplayName("회원을 찾을 수 없으면 예외가 발생한다.")
        @Test
        void tokenRegistration_not_found_member() {
            // given
            given(memberRepository.findById(1L))
                    .willReturn(Optional.empty());

            // when
            // then
            assertThatThrownBy(() -> fcmService.tokenRegistration(request))
                    .isInstanceOf(FcmException.class)
                    .hasMessageContaining(NOT_FOUND_MEMBER.getMessage());
        }
    }

    @DisplayName("알림 전송 메서드")
    @Nested
    class Method {
        private MockedStatic<FirebaseMessaging> firebaseMessagingMockedStatic;
        private final FirebaseMessaging firebaseMessagingMock = mock(FirebaseMessaging.class);

        Member member = Member.builder()
                .email("test@test.com")
                .password("test12345")
                .name("tester1")
                .build();

        FcmToken fcmToken = FcmToken.builder()
                .member(member)
                .token("fcmToken")
                .build();

        NotificationDto notificationDto = new NotificationDto(1L, "title", "body");

        @BeforeEach
        void setup() {
            firebaseMessagingMockedStatic = mockStatic(FirebaseMessaging.class);
        }

        @AfterEach
        void tearDown() {
            firebaseMessagingMockedStatic.close();
        }

        @DisplayName("FCM token으로 알림을 전송한다.")
        @Test
        void sendNotification() throws FirebaseMessagingException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            // given
            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(member));

            given(fcmTokenRepository.findByMember(member))
                    .willReturn(Optional.of(fcmToken));

            given(FirebaseMessaging.getInstance())
                    .willReturn(firebaseMessagingMock);

            ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);

            given(firebaseMessagingMock.send(captor.capture()))
                    .willReturn(any());

            // when
            fcmService.sendNotification(notificationDto);

            // then
            verify(firebaseMessagingMock).send(captor.capture());

            Message capturedMessage = captor.getValue();
            var getNotificationMethod = capturedMessage.getClass().getDeclaredMethod("getNotification");
            getNotificationMethod.setAccessible(true);

            Object invokedNotification = getNotificationMethod.invoke(capturedMessage);

            Field[] declaredFields = invokedNotification.getClass().getDeclaredFields();
            Field title = declaredFields[0];
            Field body = declaredFields[1];
            title.setAccessible(true);
            body.setAccessible(true);

            assertEquals(notificationDto.title(), title.get(invokedNotification).toString());
            assertEquals(notificationDto.body(), body.get(invokedNotification).toString());
        }

        @DisplayName("FCM 메시지 전송에 실패하면 예외가 발생한다.")
        @Test
        void sendNotification_fcm_exception() throws FirebaseMessagingException {
            // given
            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(member));

            given(fcmTokenRepository.findByMember(member))
                    .willReturn(Optional.of(fcmToken));

            given(FirebaseMessaging.getInstance())
                    .willReturn(firebaseMessagingMock);

            ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);

            given(firebaseMessagingMock.send(captor.capture()))
                    .willThrow(FirebaseMessagingException.class);

            // when
            // then
            assertThatThrownBy(() -> fcmService.sendNotification(notificationDto))
                    .isInstanceOf(FcmException.class)
                    .hasMessage(FCM_SEND_MESSAGE_EXCEPTION.getMessage());
        }
    }
}