package com.bureureung.fo.domain.user.dto;

import com.bureureung.fo.domain.user.entity.FoUser;

public record RegisterRequest(String email, String password, String nickname, String phone) {
}
