// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ParsecNetProtocolTest {

    @Test
    public void testGetProtocol() throws Exception {
        assertEquals("http", ParsecNetProtocol.HTTP.getProtocol());
        assertEquals("https", ParsecNetProtocol.HTTPS.getProtocol());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("http", ParsecNetProtocol.HTTP.toString());
        assertEquals("https", ParsecNetProtocol.HTTPS.toString());
    }
}