package com.phoenixhell.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UserExistValidator implements ConstraintValidator<UserExist, String> {
    private String username;
    //这边不好发送feign  放到controller 里面验证
    @Override
    public void initialize(UserExist constraintAnnotation) {
        username=constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return false;
    }
}
