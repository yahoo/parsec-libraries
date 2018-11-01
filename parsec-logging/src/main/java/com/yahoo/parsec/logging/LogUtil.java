// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @author ec
 */
public final class LogUtil {

    /** the delimiter. */
    private static final String KV_DELIMITER = ", ";

    /** the default stack trace element. */
    private static final int STACK_TRACE_ELEMENT = 3;

    /** millis per second. */
    private static final long MILLIS_PER_SECOND = 1000L;

    /** the object mapper. */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** the logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LogUtil.class);

    /**
     * unused constructor to pass PMD.
     */
    private LogUtil() {
        // no used
    }

    /**
     * the logger. typically, you should define your logger for your class.
     *
     * @return logger for this class
     */
    public static Logger getLogger() {
        return LOGGER;
    }

    /**
     * generate splunk logging format.
     *
     * @param tag Used to identify the source of a logging message.
     * @param message The message you would like logged.
     * @param meta meta is a key-value association map.
     * @param data information object you would like to logging, give a key-value association map is recommended.
     * @param stackTraceElement number of stack trace element you would like to specify, not to specify is recommended.
     * @return string for logging
     */
    public static String generateLog(
            String tag, String message, Map<String, String> meta, Object data, int stackTraceElement) {
        StringBuilder log = new StringBuilder();

        // logging stack trace
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length > stackTraceElement && stackTraceElements[stackTraceElement] != null) {
            StackTraceElement stack = stackTraceElements[stackTraceElement];
            log.append('[').append(stack.getClassName())
                    .append("] [").append(stack.getMethodName()).append("] [")
                    .append(stack.getLineNumber()).append("] ");
        }

        long now = System.currentTimeMillis();
        BigDecimal timeInSecond = new BigDecimal(now).divide(BigDecimal.valueOf(MILLIS_PER_SECOND));
        appendKVIfNotEmpty(log, "timestamp", String.valueOf(timeInSecond), true);

        // logging tag
        appendKVIfNotEmpty(log, "log_tag", tag, true);

        // logging msg
        appendKVIfNotEmptyEscapeQuotes(log, "log_msg", message);

        // logging meta
        if (meta != null) {
            meta.forEach((key, value) -> {
                appendKVIfNotEmptyEscapeQuotes(log, key, value);
            });
        }

        // logging data
        appendKVIfNotEmpty(log, "log_data", object2Json(data), false);

        return trimLastKVDelimiter(log.toString());
    }

    /**
     * generate splunk logging format.
     *
     * @param tag Used to identify the source of a logging message.
     * @param message The message you would like logged.
     * @param meta meta is a key-value association map.
     * @param data information object you would like to logging, give a key-value association map is recommended.
     * @return string for logging
     */
    public static String generateLog(
            String tag, String message, Map<String, String> meta, Object data) {
        return generateLog(tag, message, meta, data, STACK_TRACE_ELEMENT);
    }

    /**
     * generate splunk logging format.
     *
     * @param tag Used to identify the source of a logging message.
     * @param message The message you would like logged.
     * @param meta meta is a key-value association map.
     * @return string for logging
     */
    public static String generateLog(
            String tag, String message, Map<String, String> meta) {
        return generateLog(tag, message, meta, null, STACK_TRACE_ELEMENT);
    }

    /**
     * generate splunk logging format.
     *
     * @param tag Used to identify the source of a logging message.
     * @param message The message you would like logged.
     * @return string for logging
     */
    public static String generateLog(
            String tag, String message) {
        return generateLog(tag, message, null, null, STACK_TRACE_ELEMENT);
    }

    /**
     * append key value to string builder if value is not empty with surrounded double quotes to value option.
     *
     * @param builder string builder
     * @param key key
     * @param value value
     * @param surroundDoubleQuote surround double quote
     */
    private static void appendKVIfNotEmpty(
            StringBuilder builder, String key, String value, boolean surroundDoubleQuote) {
        if (value != null && !value.isEmpty()) {
            builder.append(key).append('=');
            if (surroundDoubleQuote) {
                builder.append('\"').append(value).append('\"');
            } else {
                builder.append(value);
            }
            builder.append(KV_DELIMITER);
        }
    }

    /**
     * append key value to string builder if value is not empty.
     *
     * @param builder string builder
     * @param key key
     * @param value value
     */
    private static void appendKVIfNotEmptyEscapeQuotes(StringBuilder builder, String key, String value) {
        if (value != null && !value.isEmpty()) {
            appendKVIfNotEmpty(builder, key, addslashes(value), true);
        }
    }

    /**
     * add slashes.
     * @param value value
     * @return escape quote string
     */
    private static String addslashes(String value) {
        // escape ', ", \
        return value.replaceAll("(['\"\\\\])", "\\\\$1");
    }

    /**
     * trim last KV_DELIMIETER.
     *
     * @param value value
     * @return trimed value
     */
    private static String trimLastKVDelimiter(String value) {
        // trim last ", "
        int index = value.lastIndexOf(KV_DELIMITER);
        int len = value.length();
        if (index != -1 && len - index == KV_DELIMITER.length()) {
            return value.substring(0, index);
        }
        return value;
    }

    /**
     * convert object to json string, just return null if convert failed.
     * @param data any object
     * @return json string
     */
    private static String object2Json(Object data) {
        if (data != null) {
            try {
                return OBJECT_MAPPER.writeValueAsString(data);
            } catch (IOException e) {
                // do nothing
                return null;
            }
        }
        return null;
    }

}
