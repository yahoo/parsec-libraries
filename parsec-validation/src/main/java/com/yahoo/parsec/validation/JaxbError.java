// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by guang001 on 11/9/15.
 */
@XmlRootElement
public class JaxbError {
    private String message;

    /**
     * Public constructor for marshaller.
     */
    public JaxbError() {
    }

    /**
     * Constructor for useful.
     * @param message error message
     */
    public JaxbError(String message) {
        this.message = message;
    }

    /**
     * Error message getter.
     * @return error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Error message setter.
     * @param message error message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
