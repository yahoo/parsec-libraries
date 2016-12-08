// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import java.util.Set;

/**
 * Created by hankting on 6/23/15.
 */
public class ParsecValidationExceptionMapperTest {

    @Test
    public void testConstraintViolationException() {
        ParsecValidationExceptionMapper mapper = new ParsecValidationExceptionMapper();

        Set<ConstraintViolation<Entity>> violations = getViolations();
        @SuppressWarnings({ "rawtypes", "unchecked" })
        ConstraintViolationException cve = new ConstraintViolationException(violations);

        Response response = mapper.toResponse(cve);
        ParsecErrorResponse e = (ParsecErrorResponse) response.getEntity();
        Assert.assertEquals(e.getError().getCode(), 0);
        Assert.assertEquals(e.getError().getMessage(), "constraint violation validate error");
        Assert.assertEquals(e.getError().getDetail().size(), 1);
    }

    private Set<ConstraintViolation<Entity>> getViolations() {
        Entity entity = new Entity();
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();

        return validator.validate(entity);
    }

    private class Entity {
        @NotNull
        public String value1;
    }

}
