// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * Created by hankting on 9/11/15.
 */
public class ParsecAsyncProgressTimerTest {

    @Test
    public void testTimer() {
        ParsecAsyncProgress progress = new ParsecAsyncProgress();

        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_STARTSINGLE);
        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_NAMELOOKUP);
        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_CONNECT);
        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_PRETRANSFER);
        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_STARTTRANSFER);
        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_TOTAL);

        assertTrue(progress.getNsLookupTime() > 0);
        assertTrue(progress.getConnectTime() > progress.getNsLookupTime());
        assertTrue(progress.getPreTransferTime() > progress.getConnectTime());
        assertTrue(progress.getStartTransferTime() > progress.getPreTransferTime());
        assertTrue(progress.getTotalTime() > progress.getStartTransferTime());
    }
}
