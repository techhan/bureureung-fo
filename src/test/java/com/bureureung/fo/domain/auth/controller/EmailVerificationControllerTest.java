package com.bureureung.fo.domain.auth.controller;

import com.bureureung.fo.domain.auth.dto.EmailSendRequest;
import com.bureureung.fo.domain.auth.dto.EmailVerifyRequest;
import com.bureureung.fo.domain.auth.service.EmailVerificationService;
import com.bureureung.fo.global.config.SecurityConfig;
import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailVerificationController.class)
@Import(SecurityConfig.class)
class EmailVerificationControllerTest {

    public static final String SEND_PATH = "/api/v1/users/email/send";
    public static final String VERIFY_PATH = "/api/v1/users/email/verify";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    private String AVAILABLE_EMAIL = "test@test.com";

    @Test
    void 이메일_인증_코드_발송을_성공한다() throws Exception {
        // given
        var request = new EmailSendRequest(AVAILABLE_EMAIL);

        // when & then
        mockMvc.perform(post(SEND_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(emailVerificationService).sendVerificationCode(AVAILABLE_EMAIL);
    }

    @Test
    void 이메일_형식이_올바르지_않으면_400을_반환한다() throws Exception {
        // given
        var request = new EmailSendRequest("test.com");

        // when & then
        mockMvc.perform(post(SEND_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT.getMessage()))
                .andExpect(jsonPath("$.errors.email").exists())
                .andDo(print());

        verify(emailVerificationService, never()).sendVerificationCode(anyString());
    }

    @Test
    void 이메일이_비어있으면_400을_반환한다() throws Exception {
        var request = new EmailSendRequest("");

        // when & then
        mockMvc.perform(post(SEND_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT.getMessage()))
                .andExpect(jsonPath("$.errors.email").exists())
                .andDo(print());

        verify(emailVerificationService, never()).sendVerificationCode(anyString());
    }

    @Test
    void 이메일_인증_코드_검증을_성공한다() throws Exception {
        // given
        var request = new EmailVerifyRequest(AVAILABLE_EMAIL, "123456");

        mockMvc.perform(post(VERIFY_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(emailVerificationService).verifyCode(AVAILABLE_EMAIL, "123456");
    }

    @Test
    void 이메일_인증_코드_검증을_실패한다() throws Exception {
        // given
        var request = new EmailVerifyRequest(AVAILABLE_EMAIL, "123456");

        doThrow(new CustomException(ErrorCode.INVALID_VERIFICATION_CODE))
                .when(emailVerificationService).verifyCode(AVAILABLE_EMAIL, "123456");

        // when & then
        mockMvc.perform(post(VERIFY_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(emailVerificationService).verifyCode(AVAILABLE_EMAIL, "123456");
    }
}