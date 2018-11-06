// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface ParsecNameResolver {
    InetAddress resolve(String name) throws UnknownHostException;
}
