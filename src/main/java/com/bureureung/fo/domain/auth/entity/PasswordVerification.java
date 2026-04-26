package com.bureureung.fo.domain.auth.entity;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "password_verify", timeToLive = 300)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordVerification {

    @Id
    private Long userId;

    private String token;
    
    public static PasswordVerification of(Long userId, String token) {
        return new PasswordVerification(userId, token);
    }
}
