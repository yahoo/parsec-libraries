// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by hankting on 9/11/15.
 */
public class ParsecAysncProgressTest {

    @Test
    public void testGetterSetter() {
        ParsecAsyncProgress progress = new ParsecAsyncProgress();
        long t = System.nanoTime();

        long startSingleTime = t;
        long connectTime = ++t;
        long nslookupTime = ++t;
        long preTransferTime = ++t;
        long startTransferTime = ++t;
        long totalTime = ++t;

        progress.setTotalTime(totalTime);
        progress.setConnectTime(connectTime);
        progress.setNsLookupTime(nslookupTime);
        progress.setPreTransferTime(preTransferTime);
        progress.setStartSingleTime(startSingleTime);
        progress.setStartTransferTime(startTransferTime);

        assertEquals(progress.getTotalTime(), totalTime);
        assertEquals(progress.getConnectTime(), connectTime);
        assertEquals(progress.getNsLookupTime(), nslookupTime);
        assertEquals(progress.getPreTransferTime(), preTransferTime);
        assertEquals(progress.getStartSingleTime(), startSingleTime);
        assertEquals(progress.getStartTransferTime(), startTransferTime);

        progress.reset();
        assertEquals(progress.getTotalTime(), 0);
        assertEquals(progress.getConnectTime(), 0);
        assertEquals(progress.getNsLookupTime(), 0);
        assertEquals(progress.getPreTransferTime(), 0);
        assertEquals(progress.getStartSingleTime(), 0);
        assertEquals(progress.getStartTransferTime(), 0);
    }
}
