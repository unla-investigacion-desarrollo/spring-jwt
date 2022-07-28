package com.project.auth.annotations.validators;

import com.project.auth.annotations.Conditional;
import com.project.auth.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static com.project.auth.constants.CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE;
import static org.springframework.util.StringUtils.isEmpty;

@Slf4j
public class ConditionalValidator implements ConstraintValidator<Conditional, Object> {

    private String[] selected;

    private String[] required;

    private String[] values;

    @Override
    public void initialize(Conditional requiredIfChecked) {
        selected = requiredIfChecked.selected();
        required = requiredIfChecked.required();
        values = requiredIfChecked.values();
    }

    @Override
    public boolean isValid(Object objectToValidate, ConstraintValidatorContext context) {
        Boolean valid = true;
        try {

            List<String> propertyValue = new ArrayList<>();
            List<String> annotationsValues = new ArrayList<>();
            for (String property : selected) {
                if (BeanUtils.getProperty(objectToValidate, property) != null) {
                    propertyValue
                            .add(BeanUtils.getProperty(objectToValidate, property).toUpperCase());
                }
            }

            if (!propertyValue.isEmpty()) {

                for (String value : values) {
                    annotationsValues.add(value.toUpperCase());
                }

                if ((propertyValue.size() == 1) &&
                        ((annotationsValues).stream().map(StringUtils::toSnackUpperCase)
                                .anyMatch(value -> propertyValue.get(0)
                                        .equals(StringUtils.toSnackUpperCase(value))))) {
                    valid = getRequiredProperties(valid, required, objectToValidate, context);

                } else if (propertyValue.size() > 1 &&
                        ((annotationsValues).containsAll(propertyValue))) {
                    valid = getRequiredProperties(valid, required, objectToValidate, context);
                }
            }
        } catch (IllegalAccessException e) {
            log.error("Accessor method is not available for class : {}, exception : {}",
                    objectToValidate.getClass().getName(), e);
            return false;
        } catch (NoSuchMethodException e) {
            log.error("Field or method is not present on class : {}, exception : {}",
                    objectToValidate.getClass().getName(), e);
            return false;
        } catch (InvocationTargetException e) {
            log.error("An exception occurred while accessing class : {}, exception : {}",
                    objectToValidate.getClass().getName(), e);
            return false;
        }
        return valid;
    }

    Boolean getRequiredProperties(Boolean valid, String[] required, Object objectToValidate,
                                  ConstraintValidatorContext context)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        for (String propName : required) {
            Object requiredValue = BeanUtils.getProperty(objectToValidate, propName);
            valid = requiredValue != null && !isEmpty(requiredValue);

            if (Boolean.FALSE.equals(valid)) {
                String message = String.format(REQUIRED_PARAM_ERROR_MESSAGE, propName);
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode(propName).addConstraintViolation();
            }
        }

        return valid;
    }
}
