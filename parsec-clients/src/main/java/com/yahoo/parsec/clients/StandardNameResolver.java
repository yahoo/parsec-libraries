// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A standard implementation of {@link ParsecNameResolver} that will use
 * {@code InetAddress.getByName} to obtain the address. It will typically return the
 * first among the addresses returned from DNS.
 */
public class StandardNameResolver implements ParsecNameResolver {

    private static final StandardNameResolver INSTANCE = new StandardNameResolver();

    /**
     * Get a shared static instance.
     *
     * @return A shared instance.
     */
    public static StandardNameResolver getInstance() {
        return INSTANCE;
    }

    @Override
    public InetAddress resolve(String name) throws UnknownHostException {
        return InetAddress.getByName(name);
    }
}
