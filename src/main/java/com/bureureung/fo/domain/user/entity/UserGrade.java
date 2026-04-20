package com.bureureung.fo.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserGrade {
    BRONZE("브론즈"),
    SILVER("실버"),
    GOLD("골드"),
    VIP("VIP");

    private final String description;
}
