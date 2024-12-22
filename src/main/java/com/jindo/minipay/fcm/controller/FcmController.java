package com.jindo.minipay.fcm.controller;

import com.jindo.minipay.fcm.dto.NotificationDto;
import com.jindo.minipay.fcm.dto.TokenRegistrationRequest;
import com.jindo.minipay.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
@RestController
public class FcmController {
    private final FcmService fcmService;

    @PostMapping("/registration")
    public ResponseEntity<Void> tokenRegistration(
            @RequestBody TokenRegistrationRequest request) {
        fcmService.tokenRegistration(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test-notification")
    public ResponseEntity<Void> testNotification(@RequestBody NotificationDto dto) {
        fcmService.sendNotification(dto);
        return ResponseEntity.ok().build();
    }
}
