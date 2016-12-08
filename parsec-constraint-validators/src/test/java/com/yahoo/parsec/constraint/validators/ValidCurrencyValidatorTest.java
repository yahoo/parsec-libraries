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

public class ValidCurrencyValidatorTest {
    private final static String message = "invalid iso 4217 currency code";

    @Test
    public void NullCurrencyTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations(null);
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void ExistedCurrencyTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations("TWD");
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void NonExistedCurrencyTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations("CCC");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void InGroupCurrencyTest() {
        Set<ConstraintViolation<Entity2>> violations = getGroupViolations(
                "CCC", IEntity2.class);
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity2> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void NotInGroupCurrencyTest() {
        Set<ConstraintViolation<Entity2>> violations = getGroupViolations(
                "CCC", IEntity3.class);
        Assert.assertEquals(violations.size(), 0);
    }

    private Set<ConstraintViolation<Entity>> getViolations(String currency) {
        Entity entity = new Entity();
        entity.currency = currency;
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();

        return validator.validate(entity);
    }

    private Set<ConstraintViolation<Entity2>>
    getGroupViolations(String currency, Class<?> clazz) {
        Entity2 entity = new Entity2();
        entity.currency = currency;
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();

        return validator.validate(entity, clazz);
    }

    private class Entity {
        @ValidCurrency
        public String currency;
    }

    private class Entity2 {
        @ValidCurrency(groups = IEntity2.class)
        public String currency;
    }

    private interface IEntity2 {
    }

    private interface IEntity3 {
    }

}
