package com.project.auth.annotations;

import com.project.auth.annotations.validators.ValueOfEnumValidator;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {ValueOfEnumValidator.class}
)
public @interface ValueOfEnum {

    Class<? extends Enum<?>> enumClass();

    String message() default "Valor no permitido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
