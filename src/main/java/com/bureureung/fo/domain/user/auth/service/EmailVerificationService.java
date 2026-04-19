package com.bureureung.fo.domain.user.auth.service;

import com.bureureung.fo.domain.user.auth.entity.EmailVerification;
import com.bureureung.fo.domain.user.auth.repository.EmailVerificationRepository;
import com.bureureung.fo.domain.user.repository.UserRepository;
import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import com.bureureung.fo.global.mail.MailContent;
import com.bureureung.fo.global.mail.MailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final MailSender mailSender;

    public void sendVerificationCode(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        EmailVerification verification = EmailVerification.issue(email);
        emailVerificationRepository.save(verification);

        mailSender.send(email, MailContent.verificationCode(verification.getCode()));
    }

    public void verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationRepository.findById(email)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_CODE_NOT_FOUND));

        if (verification.isVerified()) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        if (!verification.matches(code)) {
            throw new CustomException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        verification.verify();
        emailVerificationRepository.save(verification);
    }
}
