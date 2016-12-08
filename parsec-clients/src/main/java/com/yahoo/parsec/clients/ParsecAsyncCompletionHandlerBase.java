// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.ning.http.client.AsyncCompletionHandler;

import javax.ws.rs.core.Response;

/**
 * Simple AsyncHandler that returns {@link Response}.
 *
 * @author sho
 */
public class ParsecAsyncCompletionHandlerBase extends AsyncCompletionHandler<Response> {
    @Override
    public Response onCompleted(final com.ning.http.client.Response ningResponse) throws Exception {
        return ParsecHttpUtil.getResponse(ningResponse);
    }
}
