// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Currency;

/**
 * the {@link ValidCurrency} annotation implementation.
 *
 * @author $Id: ValidCurrencyValidator.java 23623 2012-11-19 13:27:48Z dustinl $
 * @version $Revision: 23623 $
 */
public class ValidCurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    @Override
    public void initialize(ValidCurrency constraintAnnotation) {
        // this is a empty method
    }

    @Override
    public boolean isValid(String currency, ConstraintValidatorContext context) {
        if (currency == null) {
            return true;
        }

        boolean isValid = true;
        try {
            Currency.getInstance(currency);
        } catch (IllegalArgumentException e) {
            isValid = false;
        }
        return isValid;
    }
}