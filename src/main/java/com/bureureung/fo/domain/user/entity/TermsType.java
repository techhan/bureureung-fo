package com.bureureung.fo.domain.user.entity;

import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public enum TermsType {

    TERMS("서비스 이용약관", true),
    PRIVACY("개인정보 처리방침", true),
    MARKETING("마케팅 정보 수신 동의", false),
    NIGHT_MARKETING("야간 마케팅 정보 수신 동의", false)
    ;

    private final String description;
    private final boolean required;

    public static void validateRequired(Map<TermsType, Boolean> termsMap) {
        for (TermsType type : values()) {
            if (type.required) {
                Boolean agreed = termsMap.get(type);
                if (agreed == null || !agreed) {
                    throw new CustomException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
                }
            }
        }
    }
}
