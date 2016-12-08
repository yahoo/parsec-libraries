// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

public class IpAddressValidatorTest {

    private final static String message = "invalid IP address, IP address must be IPv4 or IPv6";

    private Set<ConstraintViolation<Entity>> getViolations(String input) {
        Entity entity = new Entity();
        entity.input = input;
        Validator validator = Validation.buildDefaultValidatorFactory()
            .getValidator();

        return validator.validate(entity);
    }

    private class Entity {
        @IpAddress
        public String input;
    }

    @Test
    public void NullIpAddressTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations(null);
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void Ipv4AddressTest() {
        Set<ConstraintViolation<Entity>> violations = getViolations("127.0.0.1");
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void Ipv6AddressTest1() {
        Set<ConstraintViolation<Entity>> violations = getViolations("::1");
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void Ipv6AddressTest2() {
        Set<ConstraintViolation<Entity>> violations = getViolations("fe80::1");
        Assert.assertEquals(violations.size(), 0);
    }

    @Test
    public void InvalidIpv4AddressTest1() {
        Set<ConstraintViolation<Entity>> violations = getViolations("127.0.0.256");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void InvalidIpv4AddressTest2() {
        Set<ConstraintViolation<Entity>> violations = getViolations("127.0.0.1.1");
        Assert.assertEquals(violations.size(), 1);
    }

    @Test
    public void InvalidIpv6AddressTest1() {
        Set<ConstraintViolation<Entity>> violations = getViolations("2000:::");
        Assert.assertEquals(violations.size(), 1);
        ConstraintViolation<Entity> c = violations.iterator().next();
        Assert.assertEquals(c.getMessage(), message);
    }

    @Test
    public void InvalidIpv6AddressTest2() {
        // begins with 2002 is reserved for 6to4 tunnel which is a /48 and the last bits are derived from IPv4 address
        Set<ConstraintViolation<Entity>> violations = getViolations("2002:c0a8:101:::42");
        Assert.assertEquals(violations.size(), 1);
    }
}
