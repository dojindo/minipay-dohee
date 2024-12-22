package com.jindo.minipay.fcm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FcmTestController {
    @GetMapping("/fcm")
    public String index() {
        return "index.html";
    }
}
