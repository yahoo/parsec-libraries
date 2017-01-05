// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.parsec.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.ws.http.HTTPException;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by yamlin on 2016/12/20.
 */
public class DefaultAsyncCompletionHandlerTest {
    private Response mockResponse;
    private ObjectMapper mockObjectMapper;

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        mockResponse = mock(Response.class);
        mockObjectMapper = mock(ObjectMapper.class);
    }

    @Test
    public void testOnCompletion() throws Exception {
        when(mockResponse.getStatusCode()).thenReturn(200);
        when(mockResponse.hasResponseBody()).thenReturn(true);
        when(mockResponse.getResponseBody()).thenReturn("true");

        DefaultAsyncCompletionHandler<String> handler =
                new DefaultAsyncCompletionHandler<>(String.class);
        Assert.assertEquals(handler.onCompleted(mockResponse), "true");
    }

    @Test
    public void testOnCompletionWithProvidedMapper() throws Exception {
        when(mockResponse.getStatusCode()).thenReturn(200);
        when(mockResponse.hasResponseBody()).thenReturn(true);
        when(mockResponse.getResponseBody()).thenReturn("true");
        when(mockObjectMapper.readValue(anyString(), eq(String.class))).thenReturn("true");

        DefaultAsyncCompletionHandler<String> handler =
                new DefaultAsyncCompletionHandler<>(String.class, mockObjectMapper);
        Assert.assertEquals(handler.onCompleted(mockResponse), "true");
    }

    @Test
    public void testNoResponseBody() throws Exception {
        Set<Integer> expected = new HashSet<>();
        expected.add(201);
        when(mockResponse.getStatusCode()).thenReturn(201);
        when(mockResponse.hasResponseBody()).thenReturn(false);

        DefaultAsyncCompletionHandler<String> handler =
                new DefaultAsyncCompletionHandler<>(String.class, expected);
        Assert.assertEquals(handler.onCompleted(mockResponse), null);
    }

    @Test(expectedExceptions = HTTPException.class)
    public void testWrongStatusCodeFail() throws Exception {
        Set<Integer> expected = new HashSet<>();
        expected.add(200);
        when(mockResponse.getStatusCode()).thenReturn(500);

        DefaultAsyncCompletionHandler<String> handler =
                new DefaultAsyncCompletionHandler<>(String.class, expected);
        handler.onCompleted(mockResponse);
    }
}
