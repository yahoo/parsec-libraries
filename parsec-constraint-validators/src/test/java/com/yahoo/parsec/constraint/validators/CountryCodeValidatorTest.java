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
public class CountryCodeValidatorTest {

    private final static String message = "invalid country code, country code must follow the iso 3166 standard and in lower case";

    @Test
    public void NullCountryCodeTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations(null);

        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void ExistedUpperCountryCodeTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations("TW");

        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void NonExistedCountryCodeTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations("zx");

        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void ExistedLowerCountryCodeTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations("tw");

        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void InGroupCountryCodeTest() {
        Set<ConstraintViolation<Entity2>> violations = getGroupViolations("zx",
                IEntity2.class);

        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity2> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void NotInGroupCountryCodeTest() {
        Set<ConstraintViolation<Entity2>> violations = getGroupViolations("zx",
                IEntity3.class);

        Assert.assertEquals(violations.size(), 0);
    }

    private Set<ConstraintViolation<Entity>> getViolations(String ccode) {
        Entity entity = new Entity();
        entity.ccode = ccode;
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();

        return validator.validate(entity);
    }

    private Set<ConstraintViolation<Entity2>> getGroupViolations(String ccode,
                                                                 Class<?> clazz) {
        Entity2 entity2 = new Entity2();
        entity2.ccode = ccode;
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();

        return validator.validate(entity2, clazz);
    }


    private class Entity {
        @CountryCode
        String ccode;
    }

    private class Entity2 {
        @CountryCode(groups = IEntity2.class)
        public String ccode;
    }

    private interface IEntity2 {
    }

    private interface IEntity3 {
    }

}
