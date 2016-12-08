// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.config;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author guang001
 */
public interface ParsecConfig {
    /**
     * get string value by key.
     * @param key key
     * @return value string
     */
    String getString(String key);

    /**
     * Returns true if the {@code Config}'s root object contains no key-value
     * pairs.
     *
     * @return true if the configuration is empty
     */
    boolean isEmpty();

    /**
     * Get boolean value.
     *
     * @param path path expression
     * @return the boolean value at the requested path
     */
    boolean getBoolean(String path);

    /**
     *
     * @param path path expression
     * @return the numeric value at the requested path
     */
    Number getNumber(String path);

    /**
     * Gets the integer at the given path. If the value at the
     * path has a fractional (floating point) component, it
     * will be discarded and only the integer part will be
     * returned (it works like a "narrowing primitive conversion"
     * in the Java language specification).
     *
     * @param path path expression
     * @return the 32-bit integer value at the requested path
     */
    int getInt(String path);

    /**
     * Gets the long integer at the given path.  If the value at
     * the path has a fractional (floating point) component, it
     * will be discarded and only the integer part will be
     * returned (it works like a "narrowing primitive conversion"
     * in the Java language specification).
     *
     * @param path path expression
     * @return the 64-bit long value at the requested path
     */
    long getLong(String path);

    /**
     * @param path path expression
     * @return the floating-point value at the requested path
     */
    double getDouble(String path);

    /**
     * Gets a value as a duration in a specified
     * {@link java.util.concurrent.TimeUnit TimeUnit}. If the value is already a
     * number, then it's taken as milliseconds and then converted to the
     * requested TimeUnit; if it's a string, it's parsed understanding units
     * suffixes like "10m" or "5ns" as documented in the <a
     * href="https://github.com/typesafehub/config/blob/master/HOCON.md">the
     * spec</a>.
     *
     * @param path
     *            path expression
     * @param unit
     *            convert the return value to this time unit
     * @return the duration value at the requested path, in the given TimeUnit
     */
    long getDuration(String path, TimeUnit unit);

    /**
     * Gets a value as a java.time.Duration. If the value is
     * already a number, then it's taken as milliseconds; if it's
     * a string, it's parsed understanding units suffixes like
     * "10m" or "5ns" as documented in the <a
     * href="https://github.com/typesafehub/config/blob/master/HOCON.md">the
     * spec</a>. This method never returns null.
     *
     * @param path path expression
     * @return the duration value at the requested path
     */
    Duration getDuration(String path);

    /**
     * Gets a list value with boolean elements.  Throws if the
     * path is unset or null or not a list or contains values not
     * convertible to boolean.
     *
     * @param path
     *            the path to the list value.
     * @return the list at the path
     */
    List<Boolean> getBooleanList(String path);

    /**
     * Gets a list value with number elements.  Throws if the
     * path is unset or null or not a list or contains values not
     * convertible to number.
     *
     * @param path
     *            the path to the list value.
     * @return the list at the path
     */
    List<Number> getNumberList(String path);

    /**
     * Gets a list value with int elements.  Throws if the
     * path is unset or null or not a list or contains values not
     * convertible to int.
     *
     * @param path
     *            the path to the list value.
     * @return the list at the path
     */
    List<Integer> getIntList(String path);

    /**
     * Gets a list value with long elements.  Throws if the
     * path is unset or null or not a list or contains values not
     * convertible to long.
     *
     * @param path
     *            the path to the list value.
     * @return the list at the path
     */
    List<Long> getLongList(String path);

    /**
     * Gets a list value with double elements.  Throws if the
     * path is unset or null or not a list or contains values not
     * convertible to double.
     *
     * @param path
     *            the path to the list value.
     * @return the list at the path
     */
    List<Double> getDoubleList(String path);

    /**
     * Gets a list value with string elements.  Throws if the
     * path is unset or null or not a list or contains values not
     * convertible to string.
     *
     * @param path
     *            the path to the list value.
     * @return the list at the path
     */
    List<String> getStringList(String path);

    /**
     * Gets a list value with <code>Config</code> elements.
     * Throws if the path is unset or null or not a list or
     * contains values not convertible to <code>Config</code>.
     *
     * @param path
     *            the path to the list value.
     * @return the list at the path
     */
    List<? extends ParsecConfig> getConfigList(String path);

    /**
     * Gets a list, converting each value in the list to a duration, using the
     * same rules as {@link #getDuration(String, TimeUnit)}.
     *
     * @param path a path expression
     * @param unit time units of the returned values
     * @return list of durations, in the requested units
     */
    List<Long> getDurationList(String path, TimeUnit unit);

    /**
     * Gets a list, converting each value in the list to a duration, using the
     * same rules as {@link #getDuration(String)}.
     *
     * @param path a path expression
     * @return list of durations
     */
    List<Duration> getDurationList(String path);

    /**
     * Gets the value at the path as an unwrapped Java boxed value (
     * {@link java.lang.Boolean Boolean}, {@link java.lang.Integer Integer}, and
     * so on.
     *
     * @param path
     *            path expression
     * @return the unwrapped value at the requested path
     */
    Object getAnyRef(String path);

    /**
     * Gets a list value with any kind of elements.  Throws if the
     * path is unset or null or not a list. Each element is
     * "unwrapped"
     *
     * @param path the path to the list value.
     * @return the list at the path
     */
    List<? extends Object> getAnyRefList(String path);

    /**
     * @param path path expression
     * @return the nested {@code ParsecConfig} value at the requested path
     */
    ParsecConfig getConfig(String path);

}
