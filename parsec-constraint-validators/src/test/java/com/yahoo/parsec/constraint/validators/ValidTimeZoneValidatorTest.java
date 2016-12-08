// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

public class ValidTimeZoneValidatorTest {

    private final static String message = "invalid TimeZone";

    private Set<ConstraintViolation<Entity>> getViolations(String input) {
        Entity entity = new Entity();
        entity.input = input;
        Validator validator = Validation.buildDefaultValidatorFactory()
            .getValidator();

        return validator.validate(entity);
    }

    private class Entity {
        @ValidTimeZone
        public String input;
    }

    @Test
    public void NullTimeZoneTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations(null);
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
         public void ValidTimeZoneTest1() {
        Set<ConstraintViolation<Entity>> violations = getViolations("Asia/Taipei");
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void ValidTimeZoneTest2() {
        Set<ConstraintViolation<Entity>> violations = getViolations("UTC");
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void ValidTimeZoneTest3() {
        Set<ConstraintViolation<Entity>> violations = getViolations("US/Pacific");
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void InvalidTimeZoneTest1() {
        Set<ConstraintViolation<Entity>> violations = getViolations("GMT+8");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void InvalidTimeZoneTest2() {
        Set<ConstraintViolation<Entity>> violations = getViolations("Asia");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void InvalidTimeZoneTest3() {
        Set<ConstraintViolation<Entity>> violations = getViolations("Hawaii");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }
}
