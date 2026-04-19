package com.bureureung.fo.domain.user.dto;

import com.bureureung.fo.domain.user.entity.FoUser;

public record UserResponse(Long id, String email, String nickname) {
    public static UserResponse from(FoUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
