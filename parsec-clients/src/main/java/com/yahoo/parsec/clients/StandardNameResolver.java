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
