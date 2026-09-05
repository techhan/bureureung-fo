package com.bureureung.fo.domain.auth.controller;

import com.bureureung.fo.domain.auth.dto.LoginRequest;
import com.bureureung.fo.domain.auth.dto.LoginResponse;
import com.bureureung.fo.domain.auth.dto.VerifyPasswordRequest;
import com.bureureung.fo.domain.auth.service.AuthService;
import com.bureureung.fo.domain.user.entity.FoUser;
import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import com.bureureung.fo.global.security.JwtAccessDeniedHandler;
import com.bureureung.fo.global.security.JwtAuthenticationEntryPoint;
import com.bureureung.fo.global.security.JwtProvider;
import com.bureureung.fo.global.security.SecurityConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    JwtProvider jwtProvider;

    @MockitoBean
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    JwtAccessDeniedHandler jwtAccessDeniedHandler;

    final String LOGIN_URL = "/api/v1/auth/login";

    @Test
    void 로그인을_성공한다() throws Exception {
        // given
        FoUser user = FoUser.of("test@test.com", "abc12345!", "테스트", "01012341234");
        ReflectionTestUtils.setField(user, "id", 1L);

        LoginRequest request = new LoginRequest(user.getEmail(), user.getPassword());

        given(authService.login(any(LoginRequest.class)))
                .willReturn(LoginResponse.of("access-token", "refresh-token", user));

        // when & then
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));

        verify(authService).login(request);
    }

    @Test
    void 이메일이_비어있으면_400을_응답한다() throws Exception {
        LoginRequest request = new LoginRequest("", "abc12345!");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    void 비밀번호가_비어있으면_400을_응답한다() throws Exception {
        LoginRequest request = new LoginRequest("test@test.com", "");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    void 잘못된_이메일_또는_비밀번호를_입력하면_401을_응답한다() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "password1234!!");

        willThrow(new CustomException(ErrorCode.LOGIN_FAILED))
                .given(authService).login(any(LoginRequest.class));

        // when & then
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 토큰_재발급을_성공한다() throws Exception {
        // given
        FoUser user = FoUser.of("test@test.com", "abc12345!", "테스트", "01012341234");
        ReflectionTestUtils.setField(user, "id", 1L);

        given(authService.refresh(anyString()))
                .willReturn(LoginResponse.of("new-access-token", "new-refresh-token", user));

        // when
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"old-refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }

    @Test
    void 로그아웃을_성공한다() throws Exception {
        // given
        String accessToken = "access-token";
        Long userId = 1L;

        // JwtAuthenticationFilter가 토큰을 통과시키도록 stubbing
        given(jwtProvider.validateAndGetUserId(accessToken)).willReturn(userId);

        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        verify(authService).logout(userId);
    }

    @Test
    void 비밀번호_확인에_성공하면_인증토큰을_발급한다() throws Exception {
        String token = "access-token";
        Long userId = 1L;
        String password = "test1234!";
        var verifyPasswordRequest = new VerifyPasswordRequest(password);

        given(jwtProvider.validateAndGetUserId(token)).willReturn(userId);
        given(authService.verifyPassword(eq(userId), any(VerifyPasswordRequest.class))).willReturn(token);

        mockMvc.perform(post("/api/v1/auth/password-verify")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(token))
                .andDo(print());
    }

    @Test
    void 비밀번호_확인_시_비밀번호가_없으면_400을_반환한다() throws Exception {
        String token = "access-token";
        Long userId = 1L;

        given(jwtProvider.validateAndGetUserId(token)).willReturn(userId);

        mockMvc.perform(post("/api/v1/auth/password-verify")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyPasswordRequest(null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_E001"))
                .andDo(print());
    }
}