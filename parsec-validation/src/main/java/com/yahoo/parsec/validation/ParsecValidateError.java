// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

import org.glassfish.jersey.server.validation.ValidationError;

import java.util.List;

/**
 * Created by hankting on 6/24/15.
 */
@Deprecated
public class ParsecValidateError {
    /** The constraint error detail. */
    List<ValidationError> detail;

    /** The code. */
    private int code;

    /** The message. */
    private String message;

    /**
     * Gets the code.
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * Sets the code.
     *
     * @param code the code to set
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     *
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the detail.
     *
     * @return the detail
     */
    public List<ValidationError> getDetail() {
        return detail;
    }

    /**
     * Sets the detail.
     *
     * @param detail the detail to set
     */
    public void setDetail(List<ValidationError> detail) {
        this.detail = detail;
    }
}
