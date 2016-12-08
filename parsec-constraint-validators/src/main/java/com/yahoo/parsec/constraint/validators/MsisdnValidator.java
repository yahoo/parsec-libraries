// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the (@link Msisdn} annotation implementation.
 */
public class MsisdnValidator implements ConstraintValidator<Msisdn, String> {

    /**
     * MSISDN Regex Pattern.
     * http://en.wikipedia.org/wiki/E.164
     */
    private static final String MSISDN_PATTERN = "^[1-9][0-9]{2,14}$";

    /**
     * Compiled pattern.
     */
    private Pattern pattern;

    @Override
    public void initialize(Msisdn constraintAnnotation) {
        pattern = Pattern.compile(MSISDN_PATTERN);
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
