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

// TODO: Auto-generated Javadoc

/**
 * The javax.validation ISO 4217 currency annotation.
 *
 * @author $Id: ValidCurrency.java 23624 2012-11-19 13:32:06Z dustinl $
 * @version $Revision: 23624 $
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = ValidCurrencyValidator.class)
@Documented
@SuppressWarnings("PMD.TooManyStaticImports")
public @interface ValidCurrency {

    /**
     * the default error message.
     *
     * @return the default error message
     */
    String message() default "invalid iso 4217 currency code";

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