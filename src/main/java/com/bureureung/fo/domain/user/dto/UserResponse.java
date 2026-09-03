package com.bureureung.fo.domain.user.dto;

import com.bureureung.fo.domain.user.entity.FoUser;
import com.bureureung.fo.global.util.MaskingUtil;

public record UserResponse(
    Long id
    , String email
    , String nickname) {
    public static UserResponse from(FoUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }

    @Override
    public String toString() {
        return "UserResponse[id" + id
            + ", email" + MaskingUtil.maskEmail(email)
            + ", nickname" + nickname + "]";
    }
}
