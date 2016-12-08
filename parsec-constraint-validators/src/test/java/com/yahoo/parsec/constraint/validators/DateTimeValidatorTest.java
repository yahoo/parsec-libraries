// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author nevec
 */
public class DateTimeValidatorTest {

    private final static String message = "invalid ISO 8601 date time";

    @Test
    public void ValidDateTimeTest() {
        List<Set<ConstraintViolation<Entity>>> violations = getViolations(
                "2015-01-17T18:23:02+06:45",
                "2015-01-17T18:23:02Z",
                "2015-01-17T18:23:02-00:00",
                "2015-03-29T18:23:02+00:00"
        );

        for (Set<ConstraintViolation<Entity>> violation : violations) {
            Assert.assertEquals(violation.size(), 0);
        }
    }

    @Test
    public void InvalidDateTimeTest() {
        List<Set<ConstraintViolation<Entity>>> violations = getViolations(
                "2015-01-17T28:23:02Z",
                "2015-02-29T18:23:02Z",
                "2015-01-17T18:23:02+20:00",
                "2015-01-17T18:23:02Y"
        );

        for (Set<ConstraintViolation<Entity>> violation : violations) {
            Assert.assertEquals(violation.size(), 1);
            ConstraintViolation<Entity> c = violation.iterator().next();
            Assert.assertEquals(c.getMessage(), message);
        }
    }

    @Test
    public void InGroupInvalidDateTimeTest() {
        List<Set<ConstraintViolation<Entity>>> violations = getGroupViolations(
                IEntity2.class,
                "2015-01-17T28:23:02Z",
                "2015-02-29T18:23:02Z",
                "2015-01-17T18:23:02+20:00",
                "2015-01-17T18:23:02Y"
        );

        for (Set<ConstraintViolation<Entity>> violation : violations) {
            Assert.assertEquals(violation.size(), 1);
            ConstraintViolation<Entity> c = violation.iterator().next();
            Assert.assertEquals(c.getMessage(), message);
        }
    }

    @Test
    public void NotInGroupInvalidDateTimeTest() {
        List<Set<ConstraintViolation<Entity>>> violations = getGroupViolations(
                IEntity3.class,
                "2015-01-17T28:23:02Z",
                "2015-02-29T18:23:02Z",
                "2015-01-17T18:23:02+20:00",
                "2015-01-17T18:23:02Y"
        );

        for (Set<ConstraintViolation<Entity>> violation : violations) {
            Assert.assertEquals(violation.size(), 0);
        }
    }

    private List<Set<ConstraintViolation<Entity>>> getViolations(String... dateTimes) {
        List<Set<ConstraintViolation<Entity>>> violations = new ArrayList<>();
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();

        for (String dateTime : dateTimes) {
            Entity entity = new Entity();
            entity.dateTime = dateTime;
            violations.add(validator.validate(entity));
        }
        return violations;

    }

    private List<Set<ConstraintViolation<Entity>>>
    getGroupViolations(Class<?> clazz, String... dateTimes) {
        List<Set<ConstraintViolation<Entity>>> violations = new ArrayList<>();
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();

        for (String dateTime : dateTimes) {
            Entity2 entity = new Entity2();
            entity.dateTime = dateTime;
            violations.add(validator.validate(entity, clazz));
        }
        return violations;
    }

    private class Entity {
        @DateTime
        public String dateTime;
    }

    private class Entity2 extends Entity {
        @DateTime(groups = IEntity2.class)
        public String dateTime;
    }

    private interface IEntity2 {
    }

    private interface IEntity3 {
    }
}
