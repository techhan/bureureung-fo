package com.bureureung.fo.domain.user.service;

import com.bureureung.fo.domain.auth.dto.UserProfileResponse;
import com.bureureung.fo.domain.auth.entity.EmailVerification;
import com.bureureung.fo.domain.auth.repository.EmailVerificationRepository;
import com.bureureung.fo.domain.user.dto.RegisterRequest;
import com.bureureung.fo.domain.user.dto.UserResponse;
import com.bureureung.fo.domain.user.entity.FoUser;
import com.bureureung.fo.domain.user.entity.FoUserTerms;
import com.bureureung.fo.domain.user.entity.TermsType;
import com.bureureung.fo.domain.user.repository.UserRepository;
import com.bureureung.fo.domain.user.repository.UserTermsRepository;
import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserTermsRepository userTermsRepository;

    @Transactional
    public UserResponse register(RegisterRequest request) {

        TermsType.validateRequired(request.termsMap());

        validateRegister(request);

        String encodedPassword = passwordEncoder.encode(request.password());
        FoUser savedUser = userRepository.save(FoUser.of(request.email(), encodedPassword, request.nickname(), request.phone()));

        userTermsRepository.saveAll(FoUserTerms.of(savedUser.getId(), request.termsMap()));

        return UserResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        FoUser findUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<FoUserTerms> termsList = userTermsRepository.findByFoUserId(userId);

        return UserProfileResponse.of(findUser, termsList);
    }

    private void validateRegister(RegisterRequest request) {
        EmailVerification verification = emailVerificationRepository.findById(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_VERIFIED));

        if (!verification.isVerified()) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

}
