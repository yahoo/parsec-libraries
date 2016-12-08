// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

public class MsisdnCommValidatorTest {

    private final static String message = "invalid MsisdnComm";

    private Set<ConstraintViolation<Entity>> getViolations(String input) {
        Entity entity = new Entity();
        entity.input = input;
        Validator validator = Validation.buildDefaultValidatorFactory()
            .getValidator();

        return validator.validate(entity);
    }

    private class Entity {
        @MsisdnComm
        public String input;
    }

    @Test
    public void NullMsisdnCommTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations(null);
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void ValidMsisdnCommTest1() {
        Set<ConstraintViolation<Entity>> violations = getViolations("+886-223602888");
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void ValidMsisdnCommTest2() {
        Set<ConstraintViolation<Entity>> violations = getViolations("+1-4083495400");
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void InvalidMsisdnCommTest1() {
        Set<ConstraintViolation<Entity>> violations = getViolations("127.0.0.1");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void InvalidMsisdnCommTest2() {
        Set<ConstraintViolation<Entity>> violations = getViolations("14083495400");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void TooShortMsisdnCommTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations("+1-2");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void TooLongMsisdnCommTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations("+1-234567890123456");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }
}
