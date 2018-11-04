package com.yahoo.parsec.clients;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface ParsecNameResolver {
    InetAddress resolve(String name) throws UnknownHostException;
}
