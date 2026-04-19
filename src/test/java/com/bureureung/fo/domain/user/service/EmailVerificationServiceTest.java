package com.bureureung.fo.domain.user.service;

import com.bureureung.fo.domain.user.auth.entity.EmailVerification;
import com.bureureung.fo.domain.user.auth.service.EmailVerificationService;
import com.bureureung.fo.domain.user.auth.repository.EmailVerificationRepository;
import com.bureureung.fo.domain.user.repository.UserRepository;
import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import com.bureureung.fo.global.mail.MailContent;
import com.bureureung.fo.global.mail.MailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MailSender mailSender;

    private final String AVAILABLE_EMAIL = "test@test.com";

    @Test
    void 이미_가입_된_이메일로_인증_코드_요청_시_예외가_발생한다() {
        // given
        String email = AVAILABLE_EMAIL;
        given(userRepository.existsByEmail(email)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> emailVerificationService.sendVerificationCode(email))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL)
                .hasMessage(ErrorCode.DUPLICATE_EMAIL.getMessage());

        // 메일 발송 호출 X
        verify(mailSender, never()).send(anyString(), any());
        verify(emailVerificationRepository, never()).save(any());
    }

    @Test
    void 인증_코드_요청_시_인증_정보가_저장된다() {
        //given
        String email = AVAILABLE_EMAIL;
        given(userRepository.existsByEmail(AVAILABLE_EMAIL)).willReturn(false);

        // when
        emailVerificationService.sendVerificationCode(email);

        // then
        ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
        verify(emailVerificationRepository).save(captor.capture());

        EmailVerification saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getCode()).hasSize(6); // 인증코드 자리수
        assertThat(saved.getCode()).containsOnlyDigits(); // 숫자만인지 확인
        assertThat(saved.isVerified()).isFalse(); // 아직 검증 안됨
    }

    @Test
    void 인증_코드_요청_시_이메일이_전송된다() {
        // given
        String email = AVAILABLE_EMAIL;
        given(userRepository.existsByEmail(AVAILABLE_EMAIL)).willReturn(false);

        // when
        emailVerificationService.sendVerificationCode(email);

        // then
        ArgumentCaptor<MailContent> captor = ArgumentCaptor.forClass(MailContent.class);
        verify(mailSender).send(eq(email), captor.capture());

        MailContent sent = captor.getValue();
        assertThat(sent.body()).containsPattern("\\d{6}");
        assertThat(sent.subject()).isNotBlank();
    }

    @Test
    void 메일_전송_실패_시_예외가_발생한다() {
        // given
        String email = AVAILABLE_EMAIL;
        given(userRepository.existsByEmail(email)).willReturn(false);

        doThrow(new CustomException(ErrorCode.EMAIL_SEND_FAILED)).when(mailSender).send(anyString(), any());

        // when & then
        assertThatThrownBy(() -> emailVerificationService.sendVerificationCode(email))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_SEND_FAILED)
                .hasMessage(ErrorCode.EMAIL_SEND_FAILED.getMessage());
    }

    @Test
    void 올바른_코드를_입력하면_검증에_성공한다() {
        // given
        String email = AVAILABLE_EMAIL;
        EmailVerification verification = EmailVerification.issue(email);
        String code = verification.getCode();
        given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

        // when
        emailVerificationService.verifyCode(email, code);

        // then
        assertThat(verification.isVerified()).isTrue();
        verify(emailVerificationRepository).save(verification);
    }

    @Test
    void 인증_코드가_존재하지_않으면_예외가_발생한다() {
        // given
        String email = AVAILABLE_EMAIL;
        given(emailVerificationRepository.findById(email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyCode(email, "123456"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VERIFICATION_CODE_NOT_FOUND);

        verify(emailVerificationRepository, never()).save(any());
    }

    @Test
    void 인증_코드_불일치_시_예외가_발생한다() {
        // given
        String email = AVAILABLE_EMAIL;
        EmailVerification verification = EmailVerification.issue(email);
        String code = verification.getCode();
        given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

        // when
        assertThatThrownBy(() -> emailVerificationService.verifyCode(email, "123456"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_VERIFICATION_CODE);

        // then
        verify(emailVerificationRepository, never()).save(any());
    }

    @Test
    void 이미_인증_완료된_이메일로_재인증을_시도한다() {
        // given
        String email = AVAILABLE_EMAIL;
        EmailVerification verification = EmailVerification.issue(email);
        verification.verify();

        String code = verification.getCode();
        given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyCode(email, code))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_VERIFIED);

        verify(emailVerificationRepository, never()).save(any());
    }
}