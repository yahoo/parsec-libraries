// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface ServletRequestResponseFomatter {
    /**
     * Get request and response information then format it to string.
     *
     * This method not care about HttpServletRequest or HttpServletResponse read twice or more times, the caller
     * should process it before call this method
     *
     * @param req  http request
     * @param resp http response
     * @param additionalArgs additional arguments if need
     * @return formatted string
     */
    String format(ParsecServletRequestWrapper req, ParsecServletResponseWrapper resp, Map<String, Object> additionalArgs);
}
