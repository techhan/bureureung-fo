package com.bureureung.fo.domain.user.controller;

import com.bureureung.fo.domain.user.dto.FoUserTermsResponse;
import com.bureureung.fo.domain.user.dto.RegisterRequest;
import com.bureureung.fo.domain.user.dto.UserProfileRequest;
import com.bureureung.fo.domain.user.dto.UserProfileResponse;
import com.bureureung.fo.domain.user.dto.UserResponse;
import com.bureureung.fo.domain.user.entity.FoUserTerms;
import com.bureureung.fo.domain.user.entity.TermsType;
import com.bureureung.fo.domain.user.entity.UserGrade;
import com.bureureung.fo.domain.user.service.UserService;
import com.bureureung.fo.fixture.RegisterRequestFixture;
import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import com.bureureung.fo.global.security.JwtProvider;
import com.bureureung.fo.global.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @MockitoBean
    JwtProvider jwtProvider;

    @Autowired
    ObjectMapper objectMapper;

    final String SIGNUP_URL = "/api/v1/users/signup";

    @Test
    void 회원가입에_성공하면_201을_응답한다() throws Exception {
        // given
        var request = RegisterRequestFixture.create();
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
        var request = RegisterRequestFixture.createWithEmail("testtest.com");

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
        var request = RegisterRequestFixture.createWithPassword("1234", "1234");

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
        var request = RegisterRequestFixture.createWithPassword("abc1234!!", "bcd1234!!");

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
        var request = RegisterRequestFixture.createWithNickname("닉");

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
        var request = RegisterRequestFixture.createWithPhone("01012341");

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
        var request = RegisterRequestFixture.create();

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
        var request = RegisterRequestFixture.create();

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
        var request = RegisterRequestFixture.create();

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

    @Test
    void 본인_정보를_조회한다() throws Exception {
        String token = "access-token";
        Long userId = 1L;

        given(jwtProvider.validateAndGetUserId(token)).willReturn(userId);
        given(userService.getProfile(userId)).willReturn(new UserProfileResponse(
            userId, "test@email.com", "nick", "01012341234", null, UserGrade.BRONZE, null
        ));

        mockMvc.perform(get("/api/v1/users/me")
            .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(userId))
            .andDo(print());
    }

    @Test
    void 본인_정보를_업데이트한다() throws Exception {
        String token = "access-token";
        Long userId = 1L;
        var profile = new UserProfileRequest(token, "testNick", "01012341234",
            Map.of());
        var returnProfile = new UserProfileResponse(userId, "test@email.com", profile.nickname(), profile.phone(), null, UserGrade.BRONZE, List.of());

        given(jwtProvider.validateAndGetUserId(token)).willReturn(userId);
        given(userService.updateProfile(eq(userId), any(UserProfileRequest.class))).willReturn(returnProfile);

        mockMvc.perform(patch("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(profile)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(userId))
            .andDo(print());
    }
}