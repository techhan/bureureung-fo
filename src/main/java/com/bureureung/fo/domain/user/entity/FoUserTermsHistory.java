package com.bureureung.fo.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "fo_user_terms_history")
@EntityListeners(AuditingEntityListener.class)
public class FoUserTermsHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long foUserId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TermsType termsType;

    @Column(nullable = false)
    private boolean isAgreed;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public FoUserTermsHistory(Long userId, TermsType termsType, boolean isAgreed) {
        this.foUserId = userId;
        this.termsType = termsType;
        this.isAgreed = isAgreed;
    }
    
    public static FoUserTermsHistory of(Long userId, TermsType termsType, boolean isAgreed) {
        return new FoUserTermsHistory(userId, termsType, isAgreed);
    }
}