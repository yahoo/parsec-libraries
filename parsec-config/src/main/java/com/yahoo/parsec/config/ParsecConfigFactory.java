// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.yahoo.parsec.config.internal.TypeSafeConfWrapper;

/**
 * @author guang001
 */
public class ParsecConfigFactory {
    /**
     * environment context key.
     */
    public static final String ENV_KEY = "parsec.conf.env.context";

    /**
     * hide constructor for maven checkstyle plugin advice.
     */
    private ParsecConfigFactory() {
    }

    /**
     * prepare config for later use.
     * @return config
     */
    public static ParsecConfig load() {
        String resourceName = System.getProperty(ENV_KEY);
        if (resourceName == null) {
            throw new RuntimeException("Please set System property " + ENV_KEY + " first");
        }
        return load(resourceName);
    }

    /**
     * prepare config for later use.
     * @param envString environment string
     * @return config
     */
    public static ParsecConfig load(String envString) {
        Config config = ConfigFactory.load(envString);
        return new TypeSafeConfWrapper(config);
    }
}
