package com.bureureung.fo.domain.auth.dto;


import com.bureureung.fo.domain.user.entity.FoUser;
import com.bureureung.fo.domain.user.entity.FoUserTerms;
import com.bureureung.fo.domain.user.entity.UserGrade;

import java.util.List;

public record UserProfileResponse(
        Long id,

        String email,

        String nickname,

        String phone,

        String profileImageUrl,

        UserGrade grade,

        List<FoUserTermsResponse> terms
) {
    
    public static UserProfileResponse of(FoUser user, List<FoUserTerms> terms) {
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getNickname(), user.getPhone(),
                user.getProfileImageUrl(), user.getGrade(), terms.stream().map(FoUserTermsResponse::of).toList());
    }

}
