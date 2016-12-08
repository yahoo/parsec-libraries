// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

/**
 * Error response bean data.
 * @param <T> detail message object type
 */
public class ParsecErrorResponse<T> {
    /** the error. */
    private ParsecError<T> error;

    /**
     * Gets the error.
     *
     * @return the error
     */
    public ParsecError<T> getError() {
        return error;
    }

    /**
     * Sets the error.
     *
     * @param error the error to set
     */
    public void setError(ParsecError<T> error) {
        this.error = error;
    }
}
