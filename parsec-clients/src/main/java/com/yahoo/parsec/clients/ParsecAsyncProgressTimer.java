// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by hankting on 9/6/15.
 */
public final class ParsecAsyncProgressTimer {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ParsecAsyncProgressTimer.class);

    /**
     * enum timer op code.
     */
    public enum TimerOpCode {
        /** startsingle op. */
        TIMER_STARTSINGLE,
        /** namelookup op. */
        TIMER_NAMELOOKUP,
        /** connect op. */
        TIMER_CONNECT,
        /** pretransfer op. */
        TIMER_PRETRANSFER,
        /** starttransfer op. */
        TIMER_STARTTRANSFER,
        /** total op. */
        TIMER_TOTAL
    }

    /**
     * Unused private constructor.
     */
    private ParsecAsyncProgressTimer() {
    }

    /**
     * progress time.
     * @param progress progress data object
     * @param opCode operation code
     */
    public static void progressTime(ParsecAsyncProgress progress, TimerOpCode opCode) {
        //
        // time in microsecond
        //
        long now = System.nanoTime() / DateUtils.MILLIS_PER_SECOND;
        switch (opCode) {
            case TIMER_STARTSINGLE:
                progress.setStartSingleTime(now);
                break;
            case TIMER_NAMELOOKUP:
                progress.setNsLookupTime(now - progress.getStartSingleTime());
                break;
            case TIMER_CONNECT:
                progress.setConnectTime(now - progress.getStartSingleTime());
                break;
            case TIMER_STARTTRANSFER:
                progress.setStartTransferTime(now - progress.getStartSingleTime());
                break;
            case TIMER_PRETRANSFER:
                progress.setPreTransferTime(now - progress.getStartSingleTime());
                break;
            case TIMER_TOTAL:
                progress.setTotalTime(now - progress.getStartSingleTime());
                break;
            default:
                LOGGER.warn("opcode=" + opCode + " is not defined");
                break;
        }
    }
}
