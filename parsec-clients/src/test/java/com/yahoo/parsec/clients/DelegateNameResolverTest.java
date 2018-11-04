package com.yahoo.parsec.clients;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetAddress;

/**
 * Test class for {@link DelegateNameResolver}.
 */
public class DelegateNameResolverTest {

    private ParsecNameResolver delegate;
    private DelegateNameResolver nameResolver;

    @BeforeMethod
    public void setup() {
        delegate = Mockito.mock(ParsecNameResolver.class);
        nameResolver = new DelegateNameResolver(delegate);
    }

    @Test
    public void testResolver() throws Exception {
        InetAddress expectedAddress = InetAddress.getLoopbackAddress();
        Mockito.when(delegate.resolve("www.google.com"))
            .thenReturn(expectedAddress);

        InetAddress address = nameResolver.resolve("www.google.com");
        Assert.assertSame(expectedAddress, address);

        Mockito.verify(delegate, Mockito.times(1))
            .resolve("www.google.com");
    }

}
