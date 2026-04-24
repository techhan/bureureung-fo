package com.bureureung.fo.domain.user.entity;

import com.bureureung.fo.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Table(name = "fo_user_terms",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"fo_user_id", "terms_type"})})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoUserTerms extends BaseEntity {

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

    public static FoUserTerms of(Long foUserId, TermsType termsType, boolean isAgreed) {
        FoUserTerms foUserTerms = new FoUserTerms();
        foUserTerms.foUserId = foUserId;
        foUserTerms.termsType = termsType;
        foUserTerms.isAgreed = isAgreed;
        return foUserTerms;
    }

    public static List<FoUserTerms> of(Long foUserId, Map<TermsType, Boolean> termsMap) {
        return Arrays.stream(TermsType.values())
                .map(type -> of(foUserId, type, Boolean.TRUE.equals(termsMap.get(type))))
                .toList();
    }
}