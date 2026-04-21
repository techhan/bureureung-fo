package com.bureureung.fo.domain.auth.service;

import com.bureureung.fo.domain.auth.dto.LoginRequest;
import com.bureureung.fo.domain.auth.dto.LoginResponse;
import com.bureureung.fo.domain.auth.entity.RefreshToken;
import com.bureureung.fo.domain.auth.repository.RefreshTokenRepository;
import com.bureureung.fo.domain.user.entity.FoUser;
import com.bureureung.fo.domain.user.repository.UserRepository;
import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import com.bureureung.fo.global.security.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    JwtProvider jwtProvider;

    @InjectMocks
    AuthService authService;

    @Test
    void 로그인을_성공한다() {
        // given
        String email = "test@test.com";
        String password = "abc12345!";

        FoUser user = FoUser.of(email, "abc12345!", "테스트", "01012341234");
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);
        given(jwtProvider.createAccessToken(any(Long.class))).willReturn("access-token");
        given(jwtProvider.createRefreshToken(any(Long.class))).willReturn("refresh-token");

        LoginRequest loginRequest = new LoginRequest(email, password);

        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void 이메일이_존재하지_않는_경우_로그인에_실패한다() {
        // given
        String email = "test@test.com";
        String password = "abc12345!";

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> authService.login(new LoginRequest(email, password)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);

        // then
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void 비밀번호가_틀린_경우_로그인에_실패한다() {
        // given
        String email = "test@test.com";
        String password = "abc12345!";

        FoUser user = FoUser.of(email, "12345ab!", "테스트", "01012341234");
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, user.getPassword())).willReturn(false);

        // when
        assertThatThrownBy(() -> authService.login(new LoginRequest(email, password)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);

        // then
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void 탈퇴한_사용자는_로그인에_실패한다() {
        // given
        String email = "test@test.com";
        String password = "abc12345!";

        FoUser user = FoUser.of(email, "12345ab!", "<UNK>", "01012341234");
        user.withdraw();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        assertThatThrownBy(() -> authService.login(new LoginRequest(email, password)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);
    }
}