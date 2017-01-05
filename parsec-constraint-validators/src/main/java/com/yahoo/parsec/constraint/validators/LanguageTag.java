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
 * The javax.validation RFC 3066 LanguageTag annotation.
 *
 * @author $Id: LanguageTag.java 25920 2013-01-07 06:07:32Z hankting $
 * @version $Revision: 25920 $
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = LanguageTagValidator.class)
@Documented
@SuppressWarnings("PMD.TooManyStaticImports")
public @interface LanguageTag {

    /**
     * the default error message.
     *
     * @return the default error message
     */
    String message() default "invalid language tag, language tag must follow "
            + "the IETF BCP 47 standard";

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