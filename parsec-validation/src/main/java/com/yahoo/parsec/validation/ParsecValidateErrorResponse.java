// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

/**
 * ParsecValidateErrorResponse.
 */
@Deprecated
public class ParsecValidateErrorResponse {
    /** the error. */
    private ParsecValidateError error;

    /**
     * Gets the error.
     *
     * @return the error
     */
    public ParsecValidateError getError() {
        return error;
    }

    /**
     * Sets the error.
     *
     * @param error the error to set
     */
    public void setError(ParsecValidateError error) {
        this.error = error;
    }

}
