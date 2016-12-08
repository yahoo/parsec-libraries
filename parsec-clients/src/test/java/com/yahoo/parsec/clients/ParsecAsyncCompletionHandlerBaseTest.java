// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by hankting on 12/3/15.
 */
public class ParsecAsyncCompletionHandlerBaseTest {

    @Test
    public void testOnComplete() throws Exception {
        int mockStatusCode = 200;
        ParsecAsyncCompletionHandlerBase asyncBase = new ParsecAsyncCompletionHandlerBase();
        com.ning.http.client.Response ningResponse = mock(com.ning.http.client.Response.class);
        when(ningResponse.getStatusCode()).thenReturn(mockStatusCode);

        Response response = asyncBase.onCompleted(ningResponse);
        assertEquals(response.getStatus(), ningResponse.getStatusCode());
    }
}
