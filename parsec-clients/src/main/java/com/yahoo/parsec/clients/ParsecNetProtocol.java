// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

/**
 * Enum for net protocol.
 *
 * @author sho
 */
public enum ParsecNetProtocol {
    /**
     * http.
     */
    HTTP("http"),

    /**
     * https.
     */
    HTTPS("https");

    /**
     * protocol.
     */
    private final String protocol;

    /**
     * Constructor.
     * @param protocol protocol
     */
    ParsecNetProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * getProtocol.
     * @return string protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * toString.
     * @return string protocol
     */
    @Override
    public String toString() {
        return getProtocol();
    }
}
