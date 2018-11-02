// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.filters;

import com.ning.http.client.Request;
import com.ning.http.client.Response;

import java.util.Map;

/**
 * Created by baiyi on 11/01/2018.
 */
public interface NingRequestResponseFormatter {

    /**
     * Retrieve request and response information and format it as a string
     * @param req
     * @param resp
     * @param additionalArgs
     * @return
     */
    String format(Request req, Response resp, Map<String, Object> additionalArgs);
}
