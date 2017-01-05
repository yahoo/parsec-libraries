// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Javax.validation ISO 8601 date time annotation.
 * ref: http://tools.ietf.org/html/rfc3339#section-5.6
 * e.g.2013-03-06T11:00:00Z
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = DateTimeValidator.class)
@Documented
@SuppressWarnings("PMD.TooManyStaticImports")
public @interface DateTime {

    /**
     * Default error message.
     *
     * @return the default error message
     */
    String message() default "invalid ISO 8601 date time";

    /**
     * Class groups to apply validation for.
     *
     * @return array of groups
     */
    Class<?>[] groups() default {
    };

    /**
     * Annotation payload.
     *
     * @return array of Payload classes
     */
    Class<? extends Payload>[] payload() default {
    };
}