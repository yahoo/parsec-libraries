// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

/**
 * Created by hankting on 9/14/15.
 */
public final class ParsecClientDefine {

    /**
     * single request type.
     */
    public static final String REQUEST_SINGLE = "single";

    /**
     * single redirect retry request type.
     */
    public static final String REQUEST_SINGLE_RETRY = "single|retry";

    /**
     * host header key.
     */
    public static final String HEADER_HOST = "host";

    /**
     * content-length header key.
     */
    public static final String HEADER_CONTENT_LENGTH = "content-length";


    /**
     * unused private constructor.
     */
    private ParsecClientDefine() {
        // no use
    }
}
