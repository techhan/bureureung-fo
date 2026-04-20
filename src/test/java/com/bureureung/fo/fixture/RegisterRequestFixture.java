package com.bureureung.fo.fixture;

import com.bureureung.fo.domain.user.dto.RegisterRequest;
import com.bureureung.fo.domain.user.entity.TermsType;

import java.util.Map;

public class RegisterRequestFixture {

    private static Map<TermsType, Boolean> requiredTermsMap = Map.of(
            TermsType.TERMS, true,
            TermsType.PRIVACY, true
    );

    public static RegisterRequest create() {
        return new RegisterRequest("test@test.com",  "abc12345!","abc12345!",
                "테스트", "01012341234", requiredTermsMap);
    }

    public static RegisterRequest createWithEmail(String email) {
        return new RegisterRequest(email,  "abc12345!","abc12345!",
                "테스트", "01012341234", requiredTermsMap);
    }

    public static RegisterRequest createWithPassword(String password, String passwordConfirm) {
        return new RegisterRequest("test@test.com", password, passwordConfirm, "테스트",
                "01012341234",  requiredTermsMap);
    }

    public static RegisterRequest createWithNickname(String nickname) {
        return new RegisterRequest("test@test.com", "abc12345!","abc12345!",
                nickname, "01012341234", requiredTermsMap);
    }

    public static RegisterRequest createWithTerms(Map<TermsType, Boolean> termsMap) {
        return new RegisterRequest("test@test.com", "abc12345!", "abc12345!",
                "테스트", "01012341234", termsMap);
    }

}
