// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetAddress;

/**
 * Test class for {@link RandomNameResolver}.
 */
public class RandomNameResolverTest {

    private RandomNameResolver nameResolver;

    @BeforeMethod
    public void setup() {
        nameResolver = new RandomNameResolver();
    }

    @Test
    public void testResolver() throws Exception {
        InetAddress address = nameResolver.resolve("www.google.com");
        Assert.assertNotNull(address);
    }

}
