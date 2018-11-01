// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.filters;

import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.client.filter.FilterContext;
import com.ning.http.client.filter.FilterException;
import com.ning.http.client.filter.RequestFilter;

import java.util.function.BiPredicate;

/**
 * Log requests and responses.
 * By default it only logs put/delete/post requests and their responses.
 * This can be customized with a different BiPredicate implementation.
 *
 * Created by baiyi on 10/30/2018.
 */
public class RequestResponeLoggingFilter implements RequestFilter {

    private static BiPredicate<Request, Response> DEFAULT_LOGPREDICATE = new PostPutDeleteLoggingPredicate();

    private BiPredicate<Request, Response> predicate;

    public RequestResponeLoggingFilter(BiPredicate<Request, Response> logPredicate) {
        this.predicate = logPredicate;
    }

    public RequestResponeLoggingFilter() {
        this(DEFAULT_LOGPREDICATE);

    }

    @Override
    public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {
        return new FilterContext.FilterContextBuilder<>(ctx)
                .asyncHandler(
                        new ParsecAsyncHandlerWrapper(ctx.getAsyncHandler(), ctx.getRequest(), predicate)
                )
                .build();
    }




}
