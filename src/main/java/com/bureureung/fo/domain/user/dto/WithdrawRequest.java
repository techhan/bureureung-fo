package com.bureureung.fo.domain.user.dto;


import com.bureureung.fo.domain.user.entity.WithdrawReason;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record WithdrawRequest(
    @NotNull(message = "탈퇴 사유 선택은 필수입니다.")
    WithdrawReason reason,
    String detail) {

    @AssertTrue(message = "기타 사유 선택 시 상세 내용은 필수입니다.")
    private boolean isDetailValid() {
        if (reason != WithdrawReason.OTHER) {
            return true;
        }
        return detail != null && !detail.isBlank();
    }
}
