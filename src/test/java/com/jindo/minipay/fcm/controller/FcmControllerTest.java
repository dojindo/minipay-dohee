package com.jindo.minipay.fcm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jindo.minipay.fcm.dto.TokenRegistrationRequest;
import com.jindo.minipay.fcm.service.FcmService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FcmController.class)
class FcmControllerTest {
    @MockBean
    FcmService fcmService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @DisplayName("FCM token을 등록한다.")
    @Test
    void tokenRegistration() throws Exception {
        // given
        TokenRegistrationRequest request = new TokenRegistrationRequest(1L, "fcmToken");

        doNothing().when(fcmService).tokenRegistration(request);

        // when
        // then
        mockMvc.perform(post("/api/v1/fcm/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }
}