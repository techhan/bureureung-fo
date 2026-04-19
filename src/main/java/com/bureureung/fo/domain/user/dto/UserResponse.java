package com.bureureung.fo.domain.user.dto;

import com.bureureung.fo.domain.user.entity.FoUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String nickname;


    public static UserResponse from(FoUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
