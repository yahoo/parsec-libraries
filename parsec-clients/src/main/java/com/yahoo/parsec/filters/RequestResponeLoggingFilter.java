// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.filters;

import com.ning.http.client.Request;
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

    static final String DEFAULT_TRACE_LOGGER_NAME = "parsec.clients.reqresp_log";

    private static BiPredicate<Request, ResponseOrThrowable> DEFAULT_LOG_PREDICATE = new PostPutDeleteRequestPredicate();

    private final BiPredicate<Request, ResponseOrThrowable> predicate;
    private final NingRequestResponseFormatter formatter;
    private final String traceLoggerName;

    public RequestResponeLoggingFilter(BiPredicate<Request, ResponseOrThrowable> logPredicate,
                                       NingRequestResponseFormatter formatter,
                                       String loggerName) {
        this.predicate = logPredicate;
        this.formatter = formatter;
        this.traceLoggerName = loggerName;
    }

    public RequestResponeLoggingFilter(NingRequestResponseFormatter formatter) {
        this(DEFAULT_LOG_PREDICATE, formatter, DEFAULT_TRACE_LOGGER_NAME);
    }

    @Override
    public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {
        return new FilterContext.FilterContextBuilder<>(ctx)
                .asyncHandler(
                        new LoggingAsyncHandlerWrapper(ctx.getAsyncHandler(),
                                ctx.getRequest(),
                                predicate,
                                formatter,
                                traceLoggerName)
                )
                .build();
    }




}
