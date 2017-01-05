// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The javax.validation iso 3361 CountryCode annotation.
 *
 * @author $Id: CountryCode.java 23623 2012-11-19 13:27:48Z dustinl $
 * @version $Revision: 23623 $
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = CountryCodeValidator.class)
@Documented
@SuppressWarnings("PMD.TooManyStaticImports")
public @interface CountryCode {

    /**
     * the default error message.
     *
     * @return the default error message
     */
    String message() default "invalid country code, country code must follow "
            + "the iso 3166 standard and in lower case";

    /**
     * the class groups to apply for validation.
     *
     * @return array of validation groups
     */
    Class<?>[] groups() default {
    };

    /**
     * the annotation payload.
     *
     * @return array of Payload classes
     */
    Class<? extends Payload>[] payload() default {
    };
}