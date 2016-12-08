// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class ParsecCompletableFutureTest {

    private Future<Response> mockNingFuture;
    private Response mockNingResponse;

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        mockNingFuture = mock(Future.class);
        mockNingResponse = mock(Response.class);
    }

    @Test
    public void testCancel() throws Exception {
        // Test default value
        ParsecCompletableFuture<Response> future = new ParsecCompletableFuture<>(mockNingFuture);
        assertFalse(future.isCancelled());

        // Test cancel
        future.cancel(true);
    }

    @Test
    public void testGet() throws Exception {
        FluentCaseInsensitiveStringsMap responseHeaders = new FluentCaseInsensitiveStringsMap();

        responseHeaders.add("header1", "header1_value1");
        responseHeaders.add("header2", "header2_value1");

        List<Cookie> ningCookies = new ArrayList<>();

        ningCookies.add(new Cookie(
            "cookie1_name",
            "cookie1_value",
            false,
            null,
            "cookie1_path",
            1,
            true,
            true
        ));

        ningCookies.add(new Cookie(
            "cookie2_name",
            "cookie2_value",
            false,
            null,
            "cookie2_path",
            2,
            false,
            false
        ));

        when(mockNingResponse.getHeaders()).thenReturn(responseHeaders);
        when(mockNingResponse.getContentType()).thenReturn(MediaType.APPLICATION_JSON);
        when(mockNingResponse.getCookies()).thenReturn(ningCookies);
        when(mockNingResponse.getStatusCode()).thenReturn(200);
        when(mockNingResponse.hasResponseHeaders()).thenReturn(true);

        when(mockNingFuture.get()).thenReturn(mockNingResponse);
        when(mockNingFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(mockNingResponse);

        ParsecCompletableFuture<Response> future = new ParsecCompletableFuture<>(mockNingFuture);
        Response response = future.get();

        assertEquals(response.getContentType(), MediaType.APPLICATION_JSON);
        assertEquals(response.getHeaders().size(), 2);

        response = future.get(3, TimeUnit.SECONDS);

        assertEquals(response.getContentType(), MediaType.APPLICATION_JSON);
        assertEquals(response.getHeaders().size(), 2);
    }

    @Test
    public void testIsCancelled() throws Exception {
        // Test default value
        ParsecCompletableFuture<Response> future = new ParsecCompletableFuture<>(mockNingFuture);
        assertFalse(future.isCancelled());

        // Test delegate
        when(mockNingFuture.isCancelled()).thenReturn(true);
        assertTrue(future.isCancelled());
    }

    @Test
    public void testIsDone() throws Exception {
        // Test default value
        ParsecCompletableFuture<Response> future = new ParsecCompletableFuture<>(mockNingFuture);
        assertFalse(future.isDone());

        // Test delegate
        when(mockNingFuture.isDone()).thenReturn(true);
        assertTrue(future.isDone());
    }
}
