// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

/**
 * @author sho
 */
public class ParsecAsyncHttpClientFactoryTest {
    private ParsecAsyncHttpClient.Builder builder1, builder2;

    @BeforeMethod
    public void setUp() throws Exception {
        ParsecAsyncHttpClientFactory.clearAllInstances();
        builder1 = new ParsecAsyncHttpClient.Builder();
        builder2 = new ParsecAsyncHttpClient.Builder();
    }

    @Test
    public void testClearAllInstances() throws Exception {
        ParsecAsyncHttpClientFactory.getInstance(builder1);
        ParsecAsyncHttpClientFactory.getInstance(builder2);

        Map instanceMap = ParsecAsyncHttpClientFactory.getInstanceMap();
        assertEquals(instanceMap.size(), 2);

        ParsecAsyncHttpClientFactory.clearAllInstances();
        assertEquals(instanceMap.size(), 0);
    }

    @Test
    public void testRemoveInstance() throws Exception {
        ParsecAsyncHttpClientFactory.getInstance(builder1);
        ParsecAsyncHttpClientFactory.getInstance(builder2);

        Map instanceMap = ParsecAsyncHttpClientFactory.getInstanceMap();
        assertEquals(instanceMap.size(), 2);

        ParsecAsyncHttpClientFactory.removeInstance(builder1);
        assertEquals(instanceMap.size(), 1);
    }

    @Test
    public void testGetInstance() throws Exception {
        builder1.setMaxRedirects(4);
        builder2.setConnectTimeout(300);
        assertNotEquals(builder1, builder2);

        ParsecAsyncHttpClient client1 = ParsecAsyncHttpClientFactory.getInstance(builder1);
        assertEquals(client1.getMaxRedirects(), 4);

        ParsecAsyncHttpClientFactory.getInstance(builder1);
        Map instanceMap = ParsecAsyncHttpClientFactory.getInstanceMap();
        assertEquals(instanceMap.size(), 1);

        ParsecAsyncHttpClient client2 = ParsecAsyncHttpClientFactory.getInstance(builder2);
        assertEquals(client2.getConnectTimeout(), 300);
        assertEquals(instanceMap.size(), 2);
    }
}