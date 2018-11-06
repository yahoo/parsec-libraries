// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.parsec.filters;

import com.ning.http.client.Request;
import com.ning.http.client.filter.FilterContext;
import com.ning.http.client.filter.FilterException;
import com.yahoo.parsec.clients.ParsecClientDefine;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.function.BiPredicate;

/**
 * Created by baiyi on 11/05/2018.
 */
public class ProfilingFilter extends RequestResponeLoggingFilter {

    public ProfilingFilter(BiPredicate<Request, ResponseOrThrowable> logPredicate,
                           NingRequestResponseFormatter formatter, String loggerName) {
        super(logPredicate, formatter, loggerName);
    }

    @Override
    public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {
        Request request = ctx.getRequest();
        int requestCount = NumberUtils.toInt(request.getHeaders()
                .getFirstValue(ParsecClientDefine.HEADER_PROFILING_REQUEST_COUNT), 0);
        request.getHeaders().replaceWith(ParsecClientDefine.HEADER_PROFILING_REQUEST_COUNT,
                String.valueOf(++requestCount));

        return super.filter(ctx);
    }
}
