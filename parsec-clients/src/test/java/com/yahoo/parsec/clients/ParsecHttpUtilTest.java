// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.ning.http.client.Param;
import com.ning.http.client.cookie.Cookie;
import org.testng.annotations.Test;

import javax.ws.rs.core.NewCookie;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class ParsecHttpUtilTest {
    @Test
    public void testGetCookie() throws Exception {
        NewCookie cookie1 = ParsecHttpUtil.getCookie(new Cookie(
            "cookie1_name",
            "cookie1_value",
            false,
            null,
            "cookie1_path",
            1,
            true,
            true
        ));

        assertEquals("cookie1_name", cookie1.getName());
        assertEquals("cookie1_value", cookie1.getValue());
        assertEquals(null, cookie1.getDomain());
        assertEquals("cookie1_path", cookie1.getPath());
        assertEquals(null, cookie1.getExpiry());
        assertEquals(1, cookie1.getMaxAge());
        assertTrue(cookie1.isSecure());
        assertTrue(cookie1.isHttpOnly());
    }

    @Test
    public void testGetCookies() throws Exception {
        List<Cookie> ningCookies = new ArrayList<>();

        ningCookies.add(Cookie.newValidCookie(
            "cookie1_name",
            "cookie1_value",
            false,
            null,
            "cookie1_path",
            1,
            true,
            true
        ));

        ningCookies.add(Cookie.newValidCookie(
            "cookie2_name",
            "cookie2_value",
            false,
            null,
            "cookie2_path",
            2,
            false,
            false
        ));

        List<NewCookie> cookies = ParsecHttpUtil.getCookies(ningCookies);
        assertEquals(2, cookies.size());

        NewCookie cookie1 = cookies.get(0);
        assertEquals("cookie1_name", cookie1.getName());
        assertEquals("cookie1_value", cookie1.getValue());
        assertEquals(null, cookie1.getDomain());
        assertEquals("cookie1_path", cookie1.getPath());
        assertEquals(null, cookie1.getExpiry());
        assertEquals(1, cookie1.getMaxAge());
        assertTrue(cookie1.isSecure());
        assertTrue(cookie1.isHttpOnly());

        NewCookie cookie2 = cookies.get(1);
        assertEquals("cookie2_name", cookie2.getName());
        assertEquals("cookie2_value", cookie2.getValue());
        assertEquals(null, cookie2.getDomain());
        assertEquals("cookie2_path", cookie2.getPath());
        assertEquals(null, cookie2.getExpiry());
        assertEquals(2, cookie2.getMaxAge());
        assertFalse(cookie2.isSecure());
        assertFalse(cookie2.isHttpOnly());
    }

    @Test
    public void testGetParams() throws Exception {
        List<Param> ningParams = new ArrayList<>();
        ningParams.add(new Param("key1", "key1_value1"));
        ningParams.add(new Param("key2", "key2_value1"));

        Map<String, List<String>> params = ParsecHttpUtil.getParamsMap(ningParams);

        assertEquals(2, params.size());
        assertTrue(params.containsKey("key1") && params.containsKey("key2"));
        assertEquals("[key1_value1]", params.get("key1").toString());
        assertEquals("[key2_value1]", params.get("key2").toString());
    }

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor constructor = ParsecHttpUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void testParseCharsetFromContentType() {
        String iso8859 = StandardCharsets.ISO_8859_1.name();
        String utf8 = StandardCharsets.UTF_8.name();

        String charset = ParsecHttpUtil.parseCharsetFromContentType("application/json;charset=utf-8", iso8859);
        assertTrue(utf8.equalsIgnoreCase(charset), "actual charset is " + charset);

        charset = ParsecHttpUtil.parseCharsetFromContentType("application/json", utf8);
        assertTrue(utf8.equalsIgnoreCase(charset), "actual charset is " + charset);
    }
}
