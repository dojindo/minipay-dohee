package com.jindo.minipay.fcm.controller;

import com.jindo.minipay.fcm.dto.NotificationDto;
import com.jindo.minipay.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@Controller
public class FcmTestController {
    private final FcmService fcmService;

    @GetMapping("/fcm/test")
    public String testFcm() {
        return "fcm-test.html";
    }

    @PostMapping("/fcm/test/notification")
    public ResponseEntity<Void> testNotification(@RequestBody NotificationDto dto) {
        fcmService.sendNotification(dto);
        return ResponseEntity.ok().build();
    }
}
