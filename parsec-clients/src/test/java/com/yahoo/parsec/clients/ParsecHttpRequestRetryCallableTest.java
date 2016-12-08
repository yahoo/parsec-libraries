// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ParsecHttpRequestRetryCallableTest {
    private ParsecAsyncHttpRequest request;
    private AsyncHttpClient mockClient;
    private com.ning.http.client.Response mockResponse;
    private ListenableFuture<com.ning.http.client.Response> mockFuture;

    private void setMockClientReturnStatusCode(int returnStatusCode) throws Exception {
        when(mockResponse.getStatusCode()).thenReturn(returnStatusCode);
        when(mockFuture.get()).thenReturn(mockResponse);
        when(mockClient.executeRequest(request.getNingRequest())).thenReturn(mockFuture);
    }

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        mockResponse = mock(com.ning.http.client.Response.class);
        mockClient = mock(AsyncHttpClient.class);
        mockFuture = mock(ListenableFuture.class);
    }

    @Test
    public void testAutoRetryForHttpStatus500() throws Exception {
        request = new ParsecAsyncHttpRequest.Builder()
            .addRetryStatusCode(500)
            .build();
        setMockClientReturnStatusCode(500);

        ParsecHttpRequestRetryCallable<Response> parsecHttpRequestRetryCallable =
            new ParsecHttpRequestRetryCallable<>(mockClient, request);
        Response response = parsecHttpRequestRetryCallable.call();
        List<Response> responses = parsecHttpRequestRetryCallable.getResponses();

        // Default max retries is 3
        assertEquals(responses.size(), 4);
        assertEquals(response.getStatusCode(), 500);
    }

    @Test
    public void testCustomMaxRetries() throws Exception {
        request = new ParsecAsyncHttpRequest.Builder()
            .addRetryStatusCode(500)
            .setMaxRetries(1)
            .build();
        setMockClientReturnStatusCode(500);

        ParsecHttpRequestRetryCallable<Response> parsecHttpRequestRetryCallable =
            new ParsecHttpRequestRetryCallable<>(mockClient, request);
        Response response = parsecHttpRequestRetryCallable.call();
        List<Response> responses = parsecHttpRequestRetryCallable.getResponses();

        assertEquals(responses.size(), 2);
        assertEquals(response.getStatusCode(), 500);
    }

    @Test
    public void testNoRetryForHttpStatus200() throws Exception {
        request = new ParsecAsyncHttpRequest.Builder()
            .addRetryStatusCode(200)
            .build();
        setMockClientReturnStatusCode(200);

        ParsecHttpRequestRetryCallable<Response> parsecHttpRequestRetryCallable =
            new ParsecHttpRequestRetryCallable<>(mockClient, request);
        Response response = parsecHttpRequestRetryCallable.call();
        List<Response> responses = parsecHttpRequestRetryCallable.getResponses();

        assertEquals(responses.size(), 1);
        assertEquals(response.getStatusCode(), 200);
    }
}