// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * the {@link LanguageTag} annotation implementation.
 *
 * @author $Id: LanguageTagValidator.java 25903 2013-01-07 02:51:34Z hankting $
 * @version $Revision: 25903 $
 */
public class LanguageTagValidator implements
        ConstraintValidator<LanguageTag, String> {

    /** The country codes from {@link Locale}. */
    private static final Set<String> LANGUAGES;

    static {
        LANGUAGES = new HashSet<String>();
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale l : locales) {
            LANGUAGES.add(l.toString().replace('_', '-'));
        }
    }

    @Override
    public void initialize(LanguageTag countryCodeAnnotation) {
        // this is an empty method
    }

    @Override
    public boolean isValid(String lang, ConstraintValidatorContext context) {
        if (lang == null) {
            return true;
        }

        return LANGUAGES.contains(lang);
    }

}