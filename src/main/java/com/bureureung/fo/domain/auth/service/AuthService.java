package com.bureureung.fo.domain.auth.service;

import com.bureureung.fo.domain.auth.dto.LoginRequest;
import com.bureureung.fo.domain.auth.dto.LoginResponse;
import com.bureureung.fo.domain.auth.entity.RefreshToken;
import com.bureureung.fo.domain.auth.repository.RefreshTokenRepository;
import com.bureureung.fo.domain.user.entity.FoUser;
import com.bureureung.fo.domain.user.entity.UserStatus;
import com.bureureung.fo.domain.user.repository.UserRepository;
import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import com.bureureung.fo.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginResponse login(LoginRequest loginRequest) {
        FoUser findUser = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        if (findUser.getStatus() != UserStatus.ACTIVE) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        if (!passwordEncoder.matches(loginRequest.password(), findUser.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtProvider.createAccessToken(findUser.getId());
        String refreshToken = jwtProvider.createRefreshToken(findUser.getId());
        refreshTokenRepository.save(RefreshToken.of(findUser.getId(), refreshToken));

        return LoginResponse.of(accessToken, refreshToken, findUser);
    }
}
