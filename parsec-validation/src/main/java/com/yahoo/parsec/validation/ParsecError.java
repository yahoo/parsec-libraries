// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

import java.util.List;

/**
 * Generic error class.
 * @param <T> detail message object type
 */
public class ParsecError<T> {
    /** The constraint error detail. */
    List<T> detail;

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
    public List<T> getDetail() {
        return detail;
    }

    /**
     * Sets the detail.
     *
     * @param detail the detail to set
     */
    public void setDetail(List<T> detail) {
        this.detail = detail;
    }
}
