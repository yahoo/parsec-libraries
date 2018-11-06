// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.filters;

import com.ning.http.client.Request;

import javax.ws.rs.HttpMethod;
import java.util.function.BiPredicate;

/**
 * Only log request and response if the request method is one of POST, PUT, or DELETE
 * Created by baiyi on 10/31/2018.
 */
public class PostPutDeleteRequestPredicate implements BiPredicate<Request, ResponseOrThrowable> {
    @Override
    public boolean test(Request request, ResponseOrThrowable responseOrThrowable) {
        return (request.getMethod().equals(HttpMethod.POST)
                || request.getMethod().equals(HttpMethod.PUT)
                || request.getMethod().equals(HttpMethod.DELETE));
    }
}
