// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.filters;

import com.ning.http.client.filter.FilterContext;
import com.ning.http.client.filter.FilterException;
import com.ning.http.client.filter.RequestFilter;

/**
 * Created by baiyi on 10/30/2018.
 */
public class RequestResponeLoggingFilter implements RequestFilter {
    @Override
    public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {
        return new FilterContext.FilterContextBuilder<>(ctx)
                .asyncHandler(
                        new ParsecAsyncHandlerWrapper(ctx.getAsyncHandler(), ctx.getRequest())
                )
                .build();
    }




}
