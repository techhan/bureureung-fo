package com.bureureung.fo.domain.user.service;

import com.bureureung.fo.domain.auth.entity.EmailVerification;
import com.bureureung.fo.domain.auth.entity.PasswordVerification;
import com.bureureung.fo.domain.auth.repository.EmailVerificationRepository;
import com.bureureung.fo.domain.auth.repository.PasswordVerificationRepository;
import com.bureureung.fo.domain.auth.service.EmailVerificationService;
import com.bureureung.fo.domain.user.dto.RegisterRequest;
import com.bureureung.fo.domain.user.dto.UserProfileRequest;
import com.bureureung.fo.domain.user.dto.UserProfileResponse;
import com.bureureung.fo.domain.user.dto.UserResponse;
import com.bureureung.fo.domain.user.entity.FoUser;
import com.bureureung.fo.domain.user.entity.FoUserTerms;
import com.bureureung.fo.domain.user.entity.FoUserTermsHistory;
import com.bureureung.fo.domain.user.entity.TermsType;
import com.bureureung.fo.domain.user.repository.UserRepository;
import com.bureureung.fo.domain.user.repository.UserTermsHistoryRepository;
import com.bureureung.fo.domain.user.repository.UserTermsRepository;
import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final UserTermsRepository userTermsRepository;
    private final PasswordVerificationRepository passwordVerificationRepository;
    private final UserTermsHistoryRepository userTermsHistoryRepository;

    @Transactional
    public UserResponse register(RegisterRequest request) {

        TermsType.validateRequired(request.termsMap());
        emailVerificationService.assertVerified(request.email());
        validateDuplication(request.email(), request.nickname());

        FoUser savedUser = createUser(request);
        saveUserTerms(savedUser.getId(), request.termsMap());

        return UserResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        FoUser findUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<FoUserTerms> termsList = userTermsRepository.findByFoUserId(userId);

        return UserProfileResponse.of(findUser, termsList);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileRequest request) {
        FoUser findUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        PasswordVerification passwordVerification = passwordVerificationRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        if (!request.token().equals(passwordVerification.getToken())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        List<FoUserTerms> termsList = userTermsRepository.findByFoUserId(userId);

        findUser.update(request.nickname(), request.phone());

        termsList.forEach(t -> {
            Boolean newAgreed = request.termsMap().get(t.getTermsType());
            if (newAgreed != null && t.isAgreed() != newAgreed) {
                userTermsHistoryRepository.save(FoUserTermsHistory.of(userId, t.getTermsType(), newAgreed));
                t.updateIsAgreed(newAgreed);
            }
        });

        UserProfileResponse response = UserProfileResponse.of(findUser, termsList);

        passwordVerificationRepository.deleteById(userId);

        return response;
    }

    private void validateDuplication(String email, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    private void saveUserTerms(Long userId, Map<TermsType, Boolean> termsMap) {
        userTermsRepository.saveAll(FoUserTerms.of(userId, termsMap));
    }

    private FoUser createUser(RegisterRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        return userRepository.save(FoUser.of(request.email(), encodedPassword, request.nickname(), request.phone()));
    }

}
