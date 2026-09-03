package com.bureureung.fo.domain.user.dto;


import com.bureureung.fo.domain.user.entity.FoUser;
import com.bureureung.fo.domain.user.entity.FoUserTerms;
import com.bureureung.fo.domain.user.entity.UserGrade;

import com.bureureung.fo.global.util.MaskingUtil;
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

    @Override
    public String toString() {
        return "UserProfileResponse[id" + id
            + ", email=" + MaskingUtil.maskEmail(email)
            + ", nickname=" + nickname
            + ", phone=" + MaskingUtil.maskPhone(phone)
            + ", profileImageUrl=" + profileImageUrl
            + ", grade=" + grade
            + ", terms=" + terms + "]";
    }

}
