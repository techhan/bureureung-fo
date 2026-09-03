package com.bureureung.fo.domain.auth.dto;

import com.bureureung.fo.global.util.MaskingUtil;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "리프레시 토큰을 입력해주세요.")
        String refreshToken
) {
        @Override
        public String toString() {
                return "RefreshRequest[refreshToken=" + MaskingUtil.mask() + "]";
        }
}
