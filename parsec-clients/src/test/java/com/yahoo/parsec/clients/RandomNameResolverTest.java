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
