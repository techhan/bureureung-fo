package com.bureureung.fo.domain.user.dto;

import com.bureureung.fo.domain.user.entity.TermsType;
import jakarta.validation.constraints.*;

import java.util.Map;

public record UserProfileRequest(
        @NotBlank(message = "토큰은 필수입니다.")
        String token,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해야 합니다.")
        String nickname,

        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(
                regexp = "^010\\d{8}$",
                message = "전화번호 형식이 올바르지 않습니다."
        )
        String phone,

        @NotNull(message = "약관 동의는 필수입니다.")
        Map<TermsType, Boolean> termsMap
) {
    public static UserProfileRequest of(String token, String nickname, String phone, Map<TermsType, Boolean> termsMap) {
        return new UserProfileRequest(token, nickname, phone, termsMap);
    }
}
