package com.bureureung.fo.domain.auth.controller;

import com.bureureung.fo.domain.auth.dto.LoginRequest;
import com.bureureung.fo.domain.auth.dto.LoginResponse;
import com.bureureung.fo.domain.auth.service.AuthService;
import com.bureureung.fo.domain.user.entity.FoUser;
import com.bureureung.fo.domain.user.repository.UserRepository;
import com.bureureung.fo.domain.user.service.UserService;
import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import com.bureureung.fo.global.security.JwtProvider;
import com.bureureung.fo.global.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    UserRepository userRepository;

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
    void 이메일이_비어있으면_400을_응답한다() throws Exception{
        LoginRequest request = new LoginRequest("", "abc12345!");

        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    void 비밀번호가_비어있으면_400을_응답한다() throws Exception{
        LoginRequest request = new LoginRequest("test@test.com", "");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    void 잘묏된_이메일_또는_비밀번호를_입력하면_401을_응답한다() throws Exception {
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
}