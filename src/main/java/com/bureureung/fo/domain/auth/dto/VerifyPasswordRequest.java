package com.bureureung.fo.domain.auth.dto;

import com.bureureung.fo.global.util.MaskingUtil;
import jakarta.validation.constraints.NotBlank;

public record VerifyPasswordRequest(
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
        @Override
        public String toString() {
                return "VerifyPasswordRequest[password=" + MaskingUtil.mask() + "]";
        }
}
