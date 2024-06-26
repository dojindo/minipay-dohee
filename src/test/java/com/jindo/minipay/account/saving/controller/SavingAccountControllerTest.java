package com.jindo.minipay.account.saving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jindo.minipay.account.saving.dto.CreateSavingAccountRequest;
import com.jindo.minipay.account.saving.service.SavingAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SavingAccountController.class)
class SavingAccountControllerTest {
    @MockBean
    SavingAccountService savingAccountService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void create() throws Exception {
        // given
        CreateSavingAccountRequest request = new CreateSavingAccountRequest(1L);

        given(savingAccountService.createAccount(request))
                .willReturn(1L);

        // when
        // then
        mockMvc.perform(post("/api/v1/account/saving")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());
    }
}