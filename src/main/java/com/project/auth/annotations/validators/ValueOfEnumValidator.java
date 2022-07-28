package com.project.auth.annotations.validators;

import com.project.auth.annotations.ValueOfEnum;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, CharSequence> {

    private static final String PARAMETER_FIELD_NAME = "basePath";

    private static final Logger logger = LoggerFactory.getLogger(ValueOfEnumValidator.class);

    private List<String> acceptedValues;

    public ValueOfEnumValidator() {
    }

    @Override
    public void initialize(ValueOfEnum annotation) {
        this.acceptedValues = Stream.of(annotation.enumClass().getEnumConstants()).map(Enum::name)
                .filter(enumName -> !enumName.equals("DEFAULT"))
                .collect(Collectors.toList());
    }

    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value != null && !value.toString().isEmpty()) {
            context.disableDefaultConstraintViolation();
            String message = this.getMessage(context);
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return acceptedValues.contains(value.toString());
        } else {
            return true;
        }
    }

    private String getMessage(ConstraintValidatorContext context) {
        String parameterName;
        try {
            Field field = context.getClass().getDeclaredField(PARAMETER_FIELD_NAME);
            field.setAccessible(true);
            String node = field.get(context).toString();
            int lastNodeIndex = node.lastIndexOf(46);
            if (lastNodeIndex != -1) {
                parameterName = node.substring(lastNodeIndex + 1);
            } else {
                parameterName = node;
            }
        } catch (IllegalAccessException | NoSuchFieldException var6) {
            logger.error("Error validating enum in request", var6);
            throw new RuntimeException("Exception trying to get the enum value to validate it");
        }

        return String.format("The attribute %s is invalid. Allowed values are: %s",
                parameterName, this.acceptedValues);
    }
}
