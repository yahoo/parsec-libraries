// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * ParsecAsyncHttpClientFactory.
 *
 * @author sho
 */
public final class ParsecAsyncHttpClientFactory {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ParsecAsyncHttpClientFactory.class);

    /**
     * Client map.
     */
    private static Map<ParsecAsyncHttpClient.Builder, ParsecAsyncHttpClient> clientMap = new ConcurrentHashMap<>();

    /**
     * Unused private constructor.
     */
    private ParsecAsyncHttpClientFactory() {

    }

    /**
     * Get or create new client instance.
     *
     * @param builder client builder
     * @return ParsecAsyncHttpClient client instance
     */
    public static ParsecAsyncHttpClient getInstance(final ParsecAsyncHttpClient.Builder builder) {
        return clientMap.computeIfAbsent(builder, b -> {
            return b.build();
        });
    }

    /**
     * Remove client instance.
     *
     * @param builder client builder
     * @return ParsecAsyncHttpClient client instance if exists, or null if doesn't
     */
    public static ParsecAsyncHttpClient removeInstance(final ParsecAsyncHttpClient.Builder builder) {
        return clientMap.remove(builder);
    }

    /**
     * Clear all instances.
     */
    public static void clearAllInstances() {
        clientMap.clear();
    }

    /**
     * Get instance map (for unit testing).
     * @return Instance map
     */
    static Map getInstanceMap() {
        return clientMap;
    }
}
