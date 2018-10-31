// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.test;

import com.yahoo.parsec.clients.ParsecAsyncHttpRequest;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Created by baiyi on 10/30/2018.
 */
public class ClientTestUtils {
    public ParsecAsyncHttpRequest buildRequest(String method, int requestTimeout, URI uri,
                                               Map<String, List<String>> headers,
                                               String bodyAsJson) {

        ParsecAsyncHttpRequest.Builder builder = new ParsecAsyncHttpRequest.Builder();

        builder.setUri(uri);
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String headerKey = entry.getKey();
                for (String headerValue : entry.getValue()) {
                    builder.addHeader(headerKey, headerValue);
                }
            }
        }

        builder.setRequestTimeout(requestTimeout);

        builder.setMethod(method);
        builder.setBody(bodyAsJson).setBodyEncoding("UTF-8");
        return builder.build();
    }
}
