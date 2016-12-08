// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.config.internal;

/**
 * @author guang001
 */
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.yahoo.parsec.config.ParsecConfig;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

/**
 * @author guang001
 */
@SuppressWarnings("unchecked")
public class TypeSafeConfWrapperTest {

    private ParsecConfig getConfig() {
        Config conf = ConfigFactory.load("typesafe_wrapper_test.conf");
        return new TypeSafeConfWrapper(conf);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetString() throws Exception {
        ParsecConfig confWrapper = getConfig();
        String simpleValue = confWrapper.getString("simpleKey");
        assertEquals(simpleValue, "simpleValue");

        String driver = confWrapper.getString("db.driver");
        assertEquals(driver, "com.mysql.jdbc.Driver");

        confWrapper.getString("notExist");
    }

    @Test
    public void testIsEmpty() throws Exception {
        ParsecConfig confWrapper = getConfig();
        boolean isEmpty = confWrapper.isEmpty();
        assertEquals(isEmpty, false);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetBoolean() throws Exception {
        ParsecConfig confWrapper = getConfig();
        boolean value = confWrapper.getBoolean("boolean");
        assertEquals(value, true);

        confWrapper.getBoolean("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetNumber() throws Exception {
        ParsecConfig confWrapper = getConfig();
        Number value = confWrapper.getNumber("number");
        assertEquals(value, 123);

        confWrapper.getNumber("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetInt() throws Exception {
        ParsecConfig confWrapper = getConfig();
        int value = confWrapper.getInt("number");
        assertEquals(value, 123);

        confWrapper.getInt("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetLong() throws Exception {
        ParsecConfig confWrapper = getConfig();
        long value = confWrapper.getLong("number");
        assertEquals(value, 123L);

        confWrapper.getLong("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetDouble() throws Exception {
        ParsecConfig confWrapper = getConfig();
        double value = confWrapper.getDouble("number");
        assertEquals(value, 123.0d);

        confWrapper.getDouble("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetDurationWithUnit() throws Exception {
        ParsecConfig confWrapper = getConfig();
        long duration = confWrapper.getDuration("duration", TimeUnit.SECONDS);
        assertEquals(duration, 60 * 10);

        confWrapper.getDuration("notExist", TimeUnit.SECONDS);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetDuration() throws Exception {
        ParsecConfig confWrapper = getConfig();
        Duration duration = confWrapper.getDuration("duration");
        assertEquals(duration.getSeconds(), 60 * 10);

        confWrapper.getDuration("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetBooleanList() throws Exception {
        ParsecConfig confWrapper = getConfig();
        List<Boolean> booleanList = confWrapper.getBooleanList("booleanList");
        boolean[] expected = new boolean[]{ true, false, true };
        int idx = 0;
        for (Boolean value : booleanList) {
            assertEquals(value.booleanValue(), expected[idx++]);
        }
        confWrapper.getBooleanList("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetNumberList() throws Exception {
        int[] expected = new int[] { 1, 2, 3, 4, 5, 6 };
        ParsecConfig confWrapper = getConfig();
        List<Number> numberList = confWrapper.getNumberList("numberList");
        int idx = 0;
        for (Number value : numberList) {
            assertEquals(value, expected[idx++]);
        }
        confWrapper.getNumberList("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetIntList() throws Exception {
        int[] expected = new int[] { 1, 2, 3, 4, 5, 6 };
        ParsecConfig confWrapper = getConfig();
        List<Integer> numberList = confWrapper.getIntList("numberList");
        int idx = 0;
        for (int value : numberList) {
            assertEquals(value, expected[idx++]);
        }
        confWrapper.getIntList("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetLongList() throws Exception {
        long[] expected = new long[] { 1L, 2L, 3L, 4L, 5L, 6L };
        ParsecConfig confWrapper = getConfig();
        List<Long> numberList = confWrapper.getLongList("numberList");
        int idx = 0;
        for (long value : numberList) {
            assertEquals(value, expected[idx++]);
        }
        confWrapper.getLongList("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetDoubleList() throws Exception {
        double[] expected = new double[] { 1.0d, 2.0d, 3.0d, 4.0d, 5.0d, 6.0d };
        ParsecConfig confWrapper = getConfig();
        List<Double> numberList = confWrapper.getDoubleList("numberList");
        int idx = 0;
        for (double value : numberList) {
            assertEquals(value, expected[idx++]);
        }
        confWrapper.getDoubleList("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetStringList() throws Exception {
        ParsecConfig confWrapper = getConfig();
        String[] expectedStrings = new String[] {"abc", "456", "xyz"};
        List<String> stringList = confWrapper.getStringList("stringList");
        assertEquals(stringList.toArray(), expectedStrings);
        confWrapper.getStringList("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetConfigList() throws Exception {
        ParsecConfig confWrapper = getConfig();
        List<? extends ParsecConfig> configList = confWrapper.getConfigList("configList");
        String[] keys = new String[] {"key1","key2"};
        String[] values = new String[] {"val1","val2"};
        int idx = 0;
        for (ParsecConfig config : configList) {
            assertEquals(config.getString(keys[idx]), values[idx]);
            ++idx;
        }
        confWrapper.getConfigList("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetDurationWithUnitList() throws Exception {
        ParsecConfig confWrapper = getConfig();
        List<Long> durationList = confWrapper.getDurationList("durationList", TimeUnit.SECONDS);
        long[] expected = new long[]{ 600L, 300L };
        int idx = 0;
        for (long duration : durationList) {
            assertEquals(duration, expected[idx++]);
        }
        confWrapper.getDurationList("notExist", TimeUnit.SECONDS);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetDurationList() throws Exception {
        ParsecConfig confWrapper = getConfig();
        List<Duration> durationList = confWrapper.getDurationList("durationList");
        long[] expected = new long[]{ 600, 300 };
        int idx = 0;
        for (Duration duration : durationList) {
            assertEquals(duration.get(ChronoUnit.SECONDS), expected[idx++]);
        }
        confWrapper.getDurationList("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetAnyRef() throws Exception {
        ParsecConfig confWrapper = getConfig();
        Map<String,String> conf = (Map<String, String>)confWrapper.getAnyRef("db");
        assertEquals(conf.get("driver"), "com.mysql.jdbc.Driver");
        confWrapper.getAnyRef("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetAnyRefList() throws Exception {
        ParsecConfig confWrapper = getConfig();
        List<?> configList = confWrapper.getAnyRefList("configList");

        String[] keys = new String[] {"key1","key2"};
        String[] values = new String[] {"val1","val2"};

        int idx = 0;
        for (Object conf : configList) {
            assertTrue(conf instanceof Map);
            Map<String, String> setting = (Map<String, String>) conf;
            assertEquals(setting.get(keys[idx]), values[idx]);
            ++idx;
        }
        confWrapper.getAnyRefList("notExist");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetConfig() throws Exception {
        ParsecConfig confWrapper = getConfig();
        ParsecConfig config = confWrapper.getConfig("db");
        String username = config.getString("username");
        assertEquals("testuser", username);
        confWrapper.getConfig("notExist");
    }
}