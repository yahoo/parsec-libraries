// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.web;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import java.util.function.BiPredicate;

/**
 * Only log request and response if the request method is one of POST, PUT, or DELETE
 */
public class PostPutDeleteLoggingPredicate implements BiPredicate<HttpServletRequest, HttpServletResponse> {
    @Override
    public boolean test(HttpServletRequest request, HttpServletResponse response) {
        return (request.getMethod().equals(HttpMethod.POST)
                || request.getMethod().equals(HttpMethod.PUT)
                || request.getMethod().equals(HttpMethod.DELETE));
    }
}
