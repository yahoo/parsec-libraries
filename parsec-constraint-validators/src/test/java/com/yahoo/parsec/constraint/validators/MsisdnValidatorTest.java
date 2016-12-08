// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

public class MsisdnValidatorTest {

    private final static String message = "invalid Msisdn";

    private Set<ConstraintViolation<Entity>> getViolations(String input) {
        Entity entity = new Entity();
        entity.input = input;
        Validator validator = Validation.buildDefaultValidatorFactory()
            .getValidator();

        return validator.validate(entity);
    }

    private class Entity {
        @Msisdn
        public String input;
    }

    @Test
    public void NullMsisdnTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations(null);
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void ValidMsisdnTest1() {
        Set<ConstraintViolation<Entity>> violations = getViolations("886223602888");
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void ValidMsisdnTest2() {
        Set<ConstraintViolation<Entity>> violations = getViolations("14083495400");
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void InvalidMsisdnTest1() {
        Set<ConstraintViolation<Entity>> violations = getViolations("127.0.0.1");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void InvalidMsisdnTest2() {
        Set<ConstraintViolation<Entity>> violations = getViolations("+14083495400");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void TooShortMsisdnTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations("12");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void TooLongMsisdnTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations("1234567890123456");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }
}
