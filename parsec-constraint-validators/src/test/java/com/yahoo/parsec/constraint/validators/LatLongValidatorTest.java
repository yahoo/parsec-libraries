// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

public class LatLongValidatorTest {

    private final static String message = "invalid LatLong";

    private Set<ConstraintViolation<Entity>> getViolations(String input) {
        Entity entity = new Entity();
        entity.input = input;
        Validator validator = Validation.buildDefaultValidatorFactory()
            .getValidator();

        return validator.validate(entity);
    }

    private class Entity {
        @LatLong
        public String input;
    }

    @Test
    public void NullLatLongTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations(null);
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void SouthLatEastLongTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations("S90,E0");
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void NorthLatWestLongTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations("N90,W180");
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void InvalidLatTest1() {
        Set<ConstraintViolation<Entity>> violations = getViolations("S91,E0");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void InvalidLatTest2() {
        Set<ConstraintViolation<Entity>> violations = getViolations("-90,E0");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void InvalidLatTest3() {
        Set<ConstraintViolation<Entity>> violations = getViolations("S0.1234567,E0");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void InvalidLongTest1() {
        Set<ConstraintViolation<Entity>> violations = getViolations("S0,E181");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void InvalidLongTest2() {
        Set<ConstraintViolation<Entity>> violations = getViolations("S0,-1");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void InvalidLongTest3() {
        Set<ConstraintViolation<Entity>> violations = getViolations("S0,E0.1234567");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }
}
