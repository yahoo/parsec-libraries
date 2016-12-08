// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * the {@link CountryCode} annotation implementation, all country code must
 * be in lower case.
 *
 * @author $Id: CountryCodeValidator.java 23671 2012-11-20 07:20:44Z lcamel $
 * @version $Revision: 23671 $
 */
public class CountryCodeValidator implements
        ConstraintValidator<CountryCode, String> {

    /** The country codes from {@link Locale}. */
    private static final Set<String> COUNTRIES;

    static {
        COUNTRIES = new HashSet<String>();
        String[] ccodes = Locale.getISOCountries();
        COUNTRIES.addAll(Arrays.asList(ccodes));
    }

    @Override
    public void initialize(CountryCode countryCodeAnnotation) {
        // this is a empty method
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object,
     * javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid(String ccode, ConstraintValidatorContext context) {
        if (ccode == null) {
            return true;
        }

        for (char c : ccode.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return false;
            }
        }

        return COUNTRIES.contains(ccode.toUpperCase(Locale.ENGLISH));
    }

}