// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * {@link DateTime} annotation implementation.
 */
public class DateTimeValidator implements ConstraintValidator<DateTime, String> {

    @Override
    public void initialize(DateTime constraintAnnotation) {
        // Unused
    }

    /**
     * Whether date time string is valid ISO 8601 format.
     * ref: http://tools.ietf.org/html/rfc3339#section-5.6
     * e.g.2013-03-06T11:00:00Z
     *
     * @param dateTime date time string
     * @param context context
     * @return is valid or not
     */
    @Override
    public boolean isValid(String dateTime, ConstraintValidatorContext context) {
        if (dateTime == null) {
            return true;
        }

        try {
            OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
            return false;
        }

        return true;
    }
}