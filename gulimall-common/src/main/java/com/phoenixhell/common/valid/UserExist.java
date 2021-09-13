package com.phoenixhell.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {UserExistValidator.class})
public @interface UserExist {
    //默认message 为resource 下  ValidationMessages.properties 里面的
    //com.phoenixhell.common.valid.ListValue.message=必须提交指定的值
    String message() default "{com.phoenixhell.common.valid.ListValue.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    String  value() default "{ }" ;
}

