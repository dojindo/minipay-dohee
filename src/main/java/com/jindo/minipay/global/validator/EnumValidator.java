package com.jindo.minipay.global.validator;

import com.jindo.minipay.global.annotation.ValidEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {
    private ValidEnum validEnum;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        validEnum = constraintAnnotation;
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return true;
        }
        return Arrays.stream(validEnum.target().getEnumConstants())
                .anyMatch(o -> o.name().equalsIgnoreCase(s));
    }
}
