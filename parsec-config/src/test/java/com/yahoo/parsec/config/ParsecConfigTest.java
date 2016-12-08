// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.config;

import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author guang001
 */
public class ParsecConfigTest {
    @Test
    public void testLoadConfig() {
        String envContext = "typesafe_wrapper_test";
        System.setProperty(ParsecConfigFactory.ENV_KEY, envContext);

        ParsecConfig config = ParsecConfigFactory.load();
        String dbDriver = config.getString("db.driver");
        assertEquals(dbDriver, "com.mysql.jdbc.Driver");

        assertEquals(config.getString("common.errorMsg"), "this is a error");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testLoadConfigFail() {
        System.clearProperty(ParsecConfigFactory.ENV_KEY);
        ParsecConfigFactory.load();
    }

    @Test
    public void testPrivateCtor() throws Exception {
        Constructor constructor = ParsecConfigFactory.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
