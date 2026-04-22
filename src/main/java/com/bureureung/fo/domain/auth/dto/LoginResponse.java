package com.bureureung.fo.domain.auth.dto;

import com.bureureung.fo.domain.user.entity.FoUser;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String email,
        String nickname
) {

    public static LoginResponse of(String accessToken, String refreshToken, FoUser user) {
        return new LoginResponse(accessToken, refreshToken, user.getId(), user.getEmail(), user.getNickname());
    }
}
