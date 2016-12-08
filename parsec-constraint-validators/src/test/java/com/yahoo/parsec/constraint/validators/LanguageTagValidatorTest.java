// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * @author nevec
 */
public class LanguageTagValidatorTest {
    private final static String message = "invalid language tag, language tag must follow the IETF BCP 47 standard";

    @Test
    public void NullLanguageTagTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations(null);
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void ExistedUpperLanguageTagTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations("EN-US");

        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void NonExistedLanguageTagTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations("en-XX");

        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void ExistedLanguageTagTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations("en-US");

        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void InGroupLanguageTagTest() {
        Set<ConstraintViolation<Entity2>> violations = getGroupViolations("zx",
                IEntity2.class);

        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity2> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void NotInGroupLanguageTagTest() {
        Set<ConstraintViolation<Entity2>> violations = getGroupViolations("zx",
                IEntity3.class);

        Assert.assertEquals(violations.size(), 0);
    }

    private Set<ConstraintViolation<Entity>> getViolations(String lang) {
        Entity entity = new Entity();
        entity.lang = lang;
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();

        return validator.validate(entity);
    }

    private Set<ConstraintViolation<Entity2>> getGroupViolations(String lang,
                                                                 Class<?> clazz) {
        Entity2 entity2 = new Entity2();
        entity2.lang = lang;
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();

        return validator.validate(entity2, clazz);
    }


    private class Entity {
        @LanguageTag
        String lang;
    }

    private class Entity2 {
        @LanguageTag(groups = IEntity2.class)
        public String lang;
    }

    private interface IEntity2 {
    }

    private interface IEntity3 {
    }
}
