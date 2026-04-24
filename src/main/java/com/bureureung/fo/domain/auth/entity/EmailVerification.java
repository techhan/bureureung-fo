package com.bureureung.fo.domain.auth.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.security.SecureRandom;

@Getter
@RedisHash(value = "email_verification")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class EmailVerification {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Id
    private String email;

    private String code;

    private boolean isVerified;

    @TimeToLive
    private long ttl;

    @Builder
    private EmailVerification(String email, String code) {
        this.email = email;
        this.code = code;
        this.isVerified = false;
        this.ttl = 300; // 5분
    }

    /**
     * 인증 코드 검증
     */
    public boolean matches(String inputCode) {
        return this.code.equals(inputCode);
    }

    /**
     * 인증 완료 처리
     */
    public void verify() {
        this.isVerified = true;
    }

    public static EmailVerification issue(String email) {
        String code = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        return EmailVerification.builder().email(email).code(code).build();
    }

    /**
     * 이메일 인증 성공 시 TTL 연장
     */
    public void extendTtl() {
        this.ttl = 1800; // 30분으로 연장
    }
}
