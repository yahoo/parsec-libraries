package com.yahoo.parsec.clients;

import com.ning.http.client.NameResolver;

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
