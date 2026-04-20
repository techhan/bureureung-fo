package com.bureureung.fo.domain.user.controller;

import com.bureureung.fo.domain.user.dto.RegisterRequest;
import com.bureureung.fo.domain.user.dto.UserResponse;
import com.bureureung.fo.domain.user.service.UserService;
import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    final String SIGNUP_URL = "/api/v1/users/signup";

    @Test
    void 회원가입에_성공하면_201을_응답한다() throws Exception {
        // given
        var request = getRequest();
        var mockUser = new UserResponse(1L, request.email(), request.nickname());
        when(userService.register(any(RegisterRequest.class))).thenReturn(mockUser);

        // when
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());

        verify(userService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void 이메일_형식이_잘못되면_400을_응답한다() throws Exception {
        // given
        var request = getRequest("test", "abc12345!", "abc12345!", "테스트1", "01012341234");

        //when
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void 비밀번호_형식이_잘못되면_400을_응답한다() throws Exception {
        // given
        var request = getRequest("test@test1.com", "1234", "1234", "테스트1", "01012341234");

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void 비밀번호와_비밀번호_확인이_다르면_400을_응답한다() throws Exception {
        // given
        var request = getRequest("test@test1.com", "abc12345!", "bcc12345!", "테스트1", "01012341234");

        // when
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void 닉네임_형식이_잘못되면_400을_응답한다() throws Exception {
        // given
        var request = getRequest("test@test1.com", "abc12345!", "abc12345!", "테", "01012341234");

        // when
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void 핸드폰_형식이_잘못되면_400을_응답한다() throws Exception {
        // given
        var request = getRequest("test@test1.com", "abc12345!", "abc12345!", "테스트1", "01012341");

        // when
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void 이메일이_중복이면_409를_응답한다() throws Exception {
        // given
        var request = getRequest();

        willThrow(new CustomException(ErrorCode.DUPLICATE_EMAIL))
                .given(userService).register(any(RegisterRequest.class));

        // when & then
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    void 닉네임이_중복이면_409를_응답한다() throws Exception {
        // given
        var request = getRequest();

        willThrow(new CustomException(ErrorCode.DUPLICATE_NICKNAME))
                .given(userService).register(any(RegisterRequest.class));

        // when & then
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andDo(print());

    }

    @Test
    void 이메일_인증이_안되면_400을_응답한다() throws Exception {
        // given
        var request = getRequest();

        willThrow(new CustomException(ErrorCode.EMAIL_NOT_VERIFIED))
                .given(userService).register(any(RegisterRequest.class));

        // when & then
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.EMAIL_NOT_VERIFIED.getCode()))
                .andDo(print());
    }

    private RegisterRequest getRequest() {
        return new RegisterRequest("test@test1.com", "abc12345!", "abc12345!", "테스트1", "01012341234", Map.of());
    }

    private RegisterRequest getRequest(String email, String password, String confirmPassword, String nickname, String phone) {
        return new RegisterRequest(email, password, confirmPassword, nickname, phone, Map.of());
    }
}