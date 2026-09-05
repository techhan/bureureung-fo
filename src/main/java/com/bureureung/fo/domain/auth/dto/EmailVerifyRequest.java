package com.bureureung.fo.domain.auth.dto;

import com.bureureung.fo.global.util.MaskingUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailVerifyRequest(
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @NotBlank(message = "인증 코드는 필수입니다.")
        @Size(min = 6, max = 6, message = "인증 코드를 다시 한 번 확인해주세요.")
        String code) {

    @Override
    public String toString() {
        return "EmailVerifyRequest[email=" + MaskingUtil.maskEmail(email)
                + ", code=" + MaskingUtil.mask() + "]";
    }
}
