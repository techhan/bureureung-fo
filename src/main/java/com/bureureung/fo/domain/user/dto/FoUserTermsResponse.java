package com.bureureung.fo.domain.user.dto;

import com.bureureung.fo.domain.user.entity.FoUserTerms;
import com.bureureung.fo.domain.user.entity.TermsType;

public record FoUserTermsResponse(
        TermsType termsType,
        Boolean isAgreed
) {

    public static FoUserTermsResponse of(FoUserTerms foUserTerms) {
        return new FoUserTermsResponse(foUserTerms.getTermsType(), foUserTerms.isAgreed());
    }
}
