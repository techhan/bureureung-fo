package com.bureureung.fo.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    ACTIVE("활성"),
    DELETED("탈퇴");

    private final String description;
}
