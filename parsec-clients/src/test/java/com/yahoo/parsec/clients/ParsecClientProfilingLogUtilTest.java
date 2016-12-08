// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.client.uri.Uri;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by hankting on 9/11/15.
 */
public class ParsecClientProfilingLogUtilTest {

    @Test
    public void testLogRemoteRequest() {
        Request req = mock(Request.class);
        when(req.getUri()).thenReturn(Uri.create("http://test.yahoo.com"));
        when(req.getHeaders()).thenReturn(new FluentCaseInsensitiveStringsMap());
        Response resp = mock(Response.class);
        when(resp.getHeader(anyString())).thenReturn("mock resp header");
        when(resp.getHeader("content-length")).thenReturn("100");

        String reqStatus = "test_status";
        ParsecAsyncProgress progress = new ParsecAsyncProgress();

        ParsecClientProfilingLogUtil.logRemoteRequest(req, resp, reqStatus, progress);
        ParsecClientProfilingLogUtil.logRemoteRequest(req, null, reqStatus, progress);
    }
}
