// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.mockito.AdditionalAnswers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    private void setMockClientReturnStatusCode(int [] returnStatusCodes) throws Exception {
        List<ListenableFuture> futures = new ArrayList<>();

        for (int returnStatusCode: returnStatusCodes) {
            com.ning.http.client.Response response = mock(com.ning.http.client.Response.class);
            when(response.getStatusCode()).thenReturn(returnStatusCode);
            ListenableFuture future = mock(ListenableFuture.class);
            when(future.get()).thenReturn(response);
            futures.add(future);
        }

        when(mockClient.executeRequest(request.getNingRequest()))
            .thenAnswer(AdditionalAnswers.returnsElementsOf(futures));
    }

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        mockResponse = mock(com.ning.http.client.Response.class);
        mockFuture = mock(ListenableFuture.class);
        mockClient = mock(AsyncHttpClient.class);
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

    @Test
    public void testRetryAndSuccessBeforeReachMaxRetries() throws Exception {
        request = new ParsecAsyncHttpRequest.Builder()
            .addRetryStatusCode(408)
            .setMaxRetries(3)
            .build();
        int [] returnStatusCodes = {408, 408, 200};
        setMockClientReturnStatusCode(returnStatusCodes);

        ParsecHttpRequestRetryCallable<Response> parsecHttpRequestRetryCallable =
            new ParsecHttpRequestRetryCallable<>(mockClient, request);
        Response response = parsecHttpRequestRetryCallable.call();
        List<Response> responses = parsecHttpRequestRetryCallable.getResponses();

        assertEquals(responses.size(), 3);
        assertEquals(responses.get(0).getStatusCode(), 408);
        assertEquals(responses.get(1).getStatusCode(), 408);
        assertEquals(responses.get(2).getStatusCode(), 200);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void testRetryDelay() throws Exception {
        request = new ParsecAsyncHttpRequest.Builder()
            .addRetryStatusCode(408)
            .setMaxRetries(3)
            .build();
        int [] returnStatusCodes = {408, 408, 200};
        setMockClientReturnStatusCode(returnStatusCodes);

        ParsecHttpRequestRetryCallable.RetryDelayer retryDelayer =
            mock(ParsecHttpRequestRetryCallable.RetryDelayer.class);

        ParsecHttpRequestRetryCallable<Response> parsecHttpRequestRetryCallable =
            new ParsecHttpRequestRetryCallable<>(mockClient, request, null, retryDelayer);

        Response response = parsecHttpRequestRetryCallable.call();
        List<Response> responses = parsecHttpRequestRetryCallable.getResponses();

        assertEquals(responses.size(), 3);
        assertEquals(responses.get(0).getStatusCode(), 408);
        assertEquals(responses.get(1).getStatusCode(), 408);
        assertEquals(responses.get(2).getStatusCode(), 200);
        assertEquals(response.getStatusCode(), 200);

        verify(retryDelayer, times(2)).delay();
    }
}