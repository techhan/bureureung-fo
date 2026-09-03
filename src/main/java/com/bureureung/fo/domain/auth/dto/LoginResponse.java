package com.bureureung.fo.domain.auth.dto;

import com.bureureung.fo.domain.user.entity.FoUser;
import com.bureureung.fo.global.util.MaskingUtil;

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

    @Override
    public String toString() {
        return "LoginResponse[accessToken=" + MaskingUtil.mask()
                            + ", refreshToken=" + MaskingUtil.mask()
                            + ", userId=" + userId
                            + ", email=" + MaskingUtil.maskEmail(email)
                            + ", nickname=" + nickname + "]";
    }
}
