package com.bureureung.fo.domain.user.service;

import com.bureureung.fo.domain.auth.entity.EmailVerification;
import com.bureureung.fo.domain.auth.repository.EmailVerificationRepository;
import com.bureureung.fo.domain.user.dto.RegisterRequest;
import com.bureureung.fo.domain.user.entity.FoUser;
import com.bureureung.fo.domain.user.repository.UserRepository;
import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void 회원가입을_한다() {
        // given
        String email = "test@test.com";
        String nickname = "테스트";
        var request = getRequest(email, nickname);

        EmailVerification verification = EmailVerification.issue(email);
        verification.verify(); // 인증 완료 상태로 만들기

        given(userRepository.existsByEmail(email)).willReturn(false);
        given(userRepository.existsByNickname(nickname)).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encoded-1234");
        given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

        given(userRepository.save(any(FoUser.class))).willAnswer(invocation -> invocation.getArgument(0)); //받은 그대로 반환

        //when
        userService.register(request);

        // then
        ArgumentCaptor<FoUser> captor = ArgumentCaptor.forClass(FoUser.class);
        verify(userRepository).save(captor.capture());

        FoUser saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getNickname()).isEqualTo(nickname);
        assertThat(saved.getPassword()).isEqualTo("encoded-1234");
        assertThat(saved.getPassword()).isNotEqualTo(request.password());
    }

    @Test
    void 이메일이_중복이면_회원가입에_실패한다() {
        // given
        String email = "test@test.com";
        String nickname = "테스트";

        EmailVerification verification = EmailVerification.issue(email);
        verification.verify();

        given(userRepository.existsByEmail(email)).willReturn(true);
        given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

        var request = getRequest(email, nickname);

        // when & then
        assertThatThrownBy(() -> {
            userService.register(request);
        }).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);

        verify(userRepository, never()).save(any(FoUser.class));
    }

    @Test
    void 닉네임이_중복이면_회원가입에_실패한다() {
        // given
        String nickname = "테스트";

        String email = "test@test.com";
        EmailVerification verification = EmailVerification.issue(email);
        verification.verify();

        given(userRepository.existsByEmail(email)).willReturn(false);
        given(userRepository.existsByNickname(nickname)).willReturn(true);
        given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

        var request = getRequest(email, nickname);

        // when & then
        assertThatThrownBy(() -> {
            userService.register(request);
        }).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);

        verify(userRepository, never()).save(any(FoUser.class));
    }

    @Test
    void 이메일_인증이_안되면_회원가입을_실패한다() {
        // given
        String email = "test@test.com";
        var request = getRequest(email, "테스트");

        EmailVerification verification = EmailVerification.issue(email);
        // verify() 호출 안 함 -> 미인증 상태

        given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

        // when & then
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_NOT_VERIFIED);

        verify(userRepository, never()).save(any(FoUser.class));
    }

    private static RegisterRequest getRequest(String email, String nickname) {
        return new RegisterRequest(email, "1234", "1234", nickname, "01012341234");
    }
}