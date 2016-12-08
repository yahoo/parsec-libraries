// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.TimeZone;

/**
 * the (@link TimeZone} annotation implementation.
 */
public class ValidTimeZoneValidator implements ConstraintValidator<ValidTimeZone, String> {

    @Override
    public void initialize(ValidTimeZone constraintAnnotation) {
        // this is an empty method
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext context) {
        boolean isValid = false;

        if (null == input) {
            isValid = true;
        } else {
            if (Arrays.asList(TimeZone.getAvailableIDs()).contains(input)) {
                isValid = true;
            }
        }

        return isValid;
    }
}
