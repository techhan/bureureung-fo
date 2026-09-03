package com.bureureung.fo.domain.auth.dto;

import com.bureureung.fo.global.util.MaskingUtil;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "이메일을 입력해주세요.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password) {

        @Override
        public String toString() {
                return "LoginRequest[email=" + MaskingUtil.maskEmail(email)
                    + ", password=" + MaskingUtil.mask() + "]";
        }
}
