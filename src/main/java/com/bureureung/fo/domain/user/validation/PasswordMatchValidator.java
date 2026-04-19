package com.bureureung.fo.domain.user.validation;

import com.bureureung.fo.domain.user.dto.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest request, ConstraintValidatorContext context) {
        if(request.password() == null || request.passwordConfirm() == null) {
            return true;
        }

        return request.password().equals(request.passwordConfirm());
    }
}
