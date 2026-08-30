package com.bureureung.fo.global.security;

import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private final long accessExpiration;
    private final long refreshExpiration;
    private final SecretKey secretKey;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(long userId) {
        return createToken(userId, accessExpiration, TokenType.ACCESS);
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(long userId) {
        return createToken(userId, refreshExpiration, TokenType.REFRESH);
    }

    /**
     * 토큰에서 userId 추출
     */
    public long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 토큰 유효성 검증
     */
    public void validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 토큰 유효성 검증 + userId 추출 후 반환
     */
    public long validateAndGetUserId(String token) {
        try {
            Claims claims = parseClaims(token);
            return Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 토큰 타입이 ACCESS인지 검증한다.
     */
    public void validateAccessToken(String token) {
        if(getTokenType(token) != TokenType.ACCESS) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 토큰 타입이 REFRESH인지 검증한다.
     */
    public void validateRefreshToken(String token) {
        if(getTokenType(token) != TokenType.REFRESH) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 토큰 타입을 반환
     */
    public TokenType getTokenType(String token) {
        Claims claims = parseClaims(token);
        String typeStr = claims.get("type", String.class);
        return TokenType.valueOf(typeStr);
    }

    /**
     * 공통 토큰 생성 로직
     */
    private String createToken(long userId, long expiration, TokenType type) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .claim("type", type.name())
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
