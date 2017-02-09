// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.parsec.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;

import javax.xml.ws.http.HTTPException;
import java.util.HashSet;
import java.util.Set;

/**
 * This handler is used to map the response body to object T.
 *
 * @author yamlin
 */
public class DefaultAsyncCompletionHandler<T> extends AsyncCompletionHandler<T> {
    private final ObjectMapper objectMapper;
    private final Class<T> tClass;
    private final Set<Integer> expectedStatusCodes;

    public DefaultAsyncCompletionHandler(Class<T> tClass, Set<Integer> expected, ObjectMapper o) {
        this.objectMapper = o;
        this.tClass = tClass;

        if (expected == null || expected.isEmpty()) {
            this.expectedStatusCodes = new HashSet<>();
            this.expectedStatusCodes.add(200);
        } else {
            this.expectedStatusCodes = expected;
        }
    }

    public DefaultAsyncCompletionHandler(Class<T> tClass, Set<Integer> expected) {
        this(tClass, expected, new ObjectMapper());
    }

    public DefaultAsyncCompletionHandler(Class<T> tClass, ObjectMapper o) {
        this(tClass, null, o);
    }

    public DefaultAsyncCompletionHandler(Class<T> tClass) {
        this(tClass, null, new ObjectMapper());
    }

    @Override
    public T onCompleted(Response response) throws Exception {
        if (expectedStatusCodes.contains(response.getStatusCode())) {
            if (response.hasResponseBody()) {
                if (tClass == String.class) {
                    return tClass.cast(response.getResponseBody());
                } else {
                    return objectMapper.readValue(response.getResponseBody(), tClass);
                }
            }
            return null;
        } else {
            throw new HTTPException(response.getStatusCode());
        }
    }
}
