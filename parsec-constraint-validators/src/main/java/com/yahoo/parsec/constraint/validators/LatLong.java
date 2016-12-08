// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The javax.validation LatLong annotation.
 */
@Target({ FIELD, METHOD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = LatLongValidator.class)
public @interface LatLong {

    /**
     * the default error message.
     */
    String message() default "invalid LatLong";

    /**
     * the class groups to apply for validation.
     */
    Class<?>[] groups() default {
    };

    /**
     * the annotation payload.
     */
    Class<? extends Payload>[] payload() default {
    };
}

