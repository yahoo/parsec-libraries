// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

import java.util.List;

/**
 * Created by guang001 on 11/9/15.
 */
public final class ValidateUtil {

    /**
     * Constructor, add for MD recommend.
     */
    private ValidateUtil() {

    }

    /**
     * Build error response object.
     * @param detailMessages detail message list
     * @param errorCode error code
     * @param errorMsg error message
     * @param <T> detail message object type
     * @return error response object
     */
    public static <T> ParsecErrorResponse<T> buildErrorResponse(
            List<T> detailMessages,
            int errorCode,
            String errorMsg) {

        ParsecError<T> pve = new ParsecError<>();
        pve.setDetail(detailMessages);
        pve.setCode(errorCode);
        pve.setMessage(errorMsg);

        ParsecErrorResponse<T> pver = new ParsecErrorResponse<>();
        pver.setError(pve);

        return pver;
    }
}
