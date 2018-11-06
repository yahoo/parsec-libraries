// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetAddress;

/**
 * Test class for {@link StandardNameResolver}.
 */
public class StandardNameResolverTest {

    private StandardNameResolver nameResolver;

    @BeforeMethod
    public void setup() {
        nameResolver = new StandardNameResolver();
    }

    @Test
    public void testResolver() throws Exception {
        InetAddress address = nameResolver.resolve("www.google.com");
        Assert.assertNotNull(address);
    }

    @Test
    public void testGetInstance() {
        StandardNameResolver nameResolver1 = StandardNameResolver.getInstance();
        StandardNameResolver nameResolver2 = StandardNameResolver.getInstance();
        Assert.assertNotNull(nameResolver1);
        Assert.assertSame(nameResolver1, nameResolver2);
    }

}
