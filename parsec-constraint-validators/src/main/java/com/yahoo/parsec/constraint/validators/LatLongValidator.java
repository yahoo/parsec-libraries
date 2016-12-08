// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the (@link LatLong} annotation implementation.
 */
public class LatLongValidator implements ConstraintValidator<LatLong, String> {

    /**
     * LatLong Regex Pattern.
     */
    private static final String LATLONG_PATTERN = "^[NS]([0-8]?[0-9]|90)(\\.[0-9]{1,6})?"
        + "[,;][EW]((1?[0-7]?|[0-9]?)[0-9]|180)(\\.[0-9]{1,6})?$";

    /**
     * Compiled pattern.
     */
    private Pattern pattern;

    @Override
    public void initialize(LatLong constraintAnnotation) {
        pattern = Pattern.compile(LATLONG_PATTERN);
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext context) {
        boolean isValid = false;

        if (null == input) {
            isValid = true;
        } else {
            Matcher matcher = pattern.matcher(input);

            if (matcher.matches()) {
                isValid = true;
            }
        }

        return isValid;
    }
}
