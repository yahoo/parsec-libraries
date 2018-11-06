// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An implementation of {@link ParsecNameResolver} that will randomly resolve
 * an address among the list of results obtained from DNS.
 * It is especially useful if your target server isn't behind a load balancer,
 * and you want to balance the traffic among the available servers.
 */
public class RandomNameResolver implements ParsecNameResolver {

    @Override
    public InetAddress resolve(String name) throws UnknownHostException {
        InetAddress[] addresses = InetAddress.getAllByName(name);
        int addressIdx = ThreadLocalRandom.current().nextInt(addresses.length);
        return addresses[addressIdx];
    }
}
