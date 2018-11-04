// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;


import com.ning.http.client.multipart.ByteArrayPart;
import com.ning.http.client.multipart.FilePart;
import com.ning.http.client.multipart.Part;
import com.ning.http.client.multipart.StringPart;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.testng.Assert.*;

public class ParsecAsyncHttpRequestTest {

    private ParsecAsyncHttpRequest.Builder builder;

    @BeforeMethod
    public void setUp() throws Exception {
        builder = new ParsecAsyncHttpRequest.Builder();
    }

    @Test
    public void testAddAndGetCookies() throws Exception {
        // Test default value
        assertTrue(builder.build().getCookies().isEmpty());

        // Test add and get
        ParsecAsyncHttpRequest request = builder
            .addCookie(new NewCookie("cookie1", "cookie1_value"))
            .addCookie(new NewCookie("cookie2", "cookie2_value"))
            .build();

        List<NewCookie> getCookies = new ArrayList<>(request.getCookies());
        assertEquals(2, getCookies.size());
        assertEquals("cookie1", getCookies.get(0).getName());
        assertEquals("cookie1_value", getCookies.get(0).getValue());
    }

    @Test
    public void testAddAndGetFormParams() throws Exception {
        // Test default value
        assertTrue(builder.build().getFormParams().isEmpty());

        // Test add form param
        builder.addFormParam("key1", "key1_value1");
        Map<String, List<String>> formParams = builder.build().getFormParams();

        assertTrue(formParams.containsKey("key1"));
        assertEquals(1, formParams.size());
        assertEquals(1, formParams.get("key1").size());
        assertEquals("[key1_value1]", formParams.get("key1").toString());

        // Test append form param
        builder.addFormParam("key1", "key1_value2");
        formParams = builder.build().getFormParams();

        assertEquals(1, formParams.size());
        assertEquals(2, formParams.get("key1").size());
        assertEquals("[key1_value1, key1_value2]", formParams.get("key1").toString());
    }

    @Test
    public void testAddAndGetQueryParams() throws Exception {
        // Test default value
        assertTrue(builder.build().getQueryParams().isEmpty());

        // Test add query param
        builder.addQueryParam("key1", "key1_value1");
        builder.addQueryParam("key2", "key2_value1");
        ParsecAsyncHttpRequest request  = builder.build();
        Map<String, List<String>> queryParams = request.getQueryParams();

        assertTrue(queryParams.containsKey("key1"));
        assertTrue(queryParams.containsKey("key2"));
        assertEquals(2, queryParams.size());
        assertEquals(1, queryParams.get("key1").size());
        assertEquals("[key1_value1]", queryParams.get("key1").toString());
        assertEquals("http://localhost?key1=key1_value1&key2=key2_value1", request.getUrl());

        // Test append query param
        builder.addQueryParam("key1", "key1_value2");
        request = builder.build();
        queryParams = request.getQueryParams();

        assertTrue(queryParams.containsKey("key1"));
        assertTrue(queryParams.containsKey("key2"));
        assertEquals(2, queryParams.size());
        assertEquals(2, queryParams.get("key1").size());
        assertEquals("[key1_value1, key1_value2]", queryParams.get("key1").toString());
        assertEquals("http://localhost?key1=key1_value1&key2=key2_value1&key1=key1_value2", request.getUrl());

        // Test clear query params
        assertTrue(builder.setQueryParams(null).build().getQueryParams().isEmpty());
    }

    @Test
    public void testAddRemoveAndGetRetryStatusCodes() throws Exception {
        // Test default value
        assertTrue(builder.build().getRetryStatusCodes().isEmpty());

        // Test add and get
        List<Integer> retryStatusCodes = builder
            .addRetryStatusCode(502).addRetryStatusCode(503).build().getRetryStatusCodes();
        assertEquals(2, retryStatusCodes.size());
        assertTrue(retryStatusCodes.contains(502));
        assertTrue(retryStatusCodes.contains(503));

        // Test filter 200
        retryStatusCodes = builder.addRetryStatusCode(200).build().getRetryStatusCodes();
        assertEquals(2, retryStatusCodes.size());
        assertFalse(retryStatusCodes.contains(200));

        retryStatusCodes = builder.removeRetryStatusCode(503).build().getRetryStatusCodes();
        assertEquals(1, retryStatusCodes.size());
        assertTrue(retryStatusCodes.contains(502));
        assertFalse(retryStatusCodes.contains(503));
    }

    @Test
    public void testAddSetAndGetHeaderString() throws Exception {
        // Test get non existing header
        assertEquals(null, builder.build().getHeaderString("non-existing-header"));

        // Test adding and appending header values
        builder.addHeader("header1", "header1_value1")
            .addHeader("header1", "header1_value2");
        assertEquals("header1_value1, header1_value2", builder.build().getHeaderString("header1"));

        // Test setting/overwriting header value
        builder.setHeader("header1", "header1_value3");
        assertEquals("header1_value3", builder.build().getHeaderString("header1"));
    }

    @Test
    public void testAddSetAndGetHeaders() throws Exception {
        System.out.println(builder.build().getHeaders());
        // Test default value
        assertEquals(1, builder.build().getHeaders().size());
        //assertTrue(builder.build().getHeaders().isEmpty());

        // Test adding and appending header values
        builder.addHeader("header1", "header1_value1")
            .addHeader("header1", "header1_value2")
            .addHeader("header2", "header2_value1");

        Map<String, List<String>> headers = builder.build().getHeaders();

        assertEquals(3, headers.size());
        assertEquals(2, headers.get("header1").size());
        assertEquals("header1_value2", headers.get("header1").get(1));
        assertEquals("header2_value1", headers.get("header2").get(0));

        assertTrue(headers.containsKey("header1"));
        assertTrue(headers.containsKey("header2"));
        assertTrue(headers.get("header1").contains("header1_value1"));
        assertTrue(headers.get("header1").contains("header1_value2"));
        assertTrue(headers.get("header2").contains("header2_value1"));
        assertFalse(headers.get("header1").contains("header2_value1"));
        assertFalse(headers.get("header2").contains("header1_value1"));

        // Test setting/overwriting headers
        Map<String, Collection<String>> newHeaders = new HashMap<>();

        List<String> header3Values = new ArrayList<>(),
            header4Values = new ArrayList<>();

        header3Values.add("header3_value1");
        header3Values.add("header3_value2");
        header4Values.add("header4_value1");

        newHeaders.put("header3", header3Values);
        newHeaders.put("header4", header4Values);

        builder.setHeaders(newHeaders);
        headers = builder.build().getHeaders();

        assertEquals(3, headers.size());
        assertEquals(2, headers.get("header3").size());
        assertEquals("header3_value2", headers.get("header3").get(1));
        assertEquals("header4_value1", headers.get("header4").get(0));

        assertTrue(headers.containsKey("header3"));
        assertTrue(headers.containsKey("header4"));
        assertTrue(headers.get("header3").contains("header3_value1"));
        assertTrue(headers.get("header3").contains("header3_value2"));
        assertTrue(headers.get("header4").contains("header4_value1"));
        assertFalse(headers.get("header3").contains("header4_value1"));
        assertFalse(headers.get("header4").contains("header3_value1"));

        // Test clear headers
        builder.setHeaders(null);
        headers = builder.build().getHeaders();
        //assertTrue(headers.isEmpty());
        assertEquals(1, builder.build().getHeaders().size());
    }

    @Test
    public void testGetMethod() throws Exception {
        // Test default value
        assertEquals("GET", builder.build().getMethod());

        // Test set and get
        assertEquals("POST", builder.setMethod("POST").build().getMethod());
    }

    @Test
    public void testGetNingRequest() throws Exception {
        assertNotNull(builder.build().getNingRequest());
    }

    @Test(expectedExceptions = InvocationTargetException.class)
    public void testPackageLevelConstructor() throws Exception {
        Constructor constructor = ParsecAsyncHttpRequest.class.getDeclaredConstructor();
        assertEquals(constructor.getModifiers(), 0);
        constructor.newInstance();
    }

    @Test
    public void testSetAndGetBody() throws Exception {
        // Test default value
        assertEquals(null, builder.build().getBody());

        // Test set and get
        assertEquals("body-data", builder.setBody("body-data").build().getBody());
    }

    @Test
    public void testSetAndGetBinaryBody() {
        byte[] binData = new byte[] { 0xa, 0x1, 0x10 };
        assertEquals(binData, builder.setBody(binData).build().getBodyByte());
    }

    @Test
    public void testSetAndGetBodyEncoding() throws Exception {
        // Test default value
        assertEquals(null, builder.build().getBodyEncoding());

        // Test set and get
        assertEquals("UTF-8", builder.setBodyEncoding("UTF-8").build().getBodyEncoding());
    }

    @Test
    public void testSetAndGetContentLength() throws Exception {
        // Test default value
        assertEquals(0, builder.build().getContentLength());

        // Test set and get
        assertEquals(123, builder.setContentLength(123).build().getContentLength());
    }

    @Test
    public void testSetAndGetCookies() throws Exception {
        // Test default value
        assertTrue(builder.build().getCookies().isEmpty());

        // Test set and get
        List<NewCookie> setCookies = new ArrayList<>();
        setCookies.add(new NewCookie("cookie1", "cookie1_value"));
        setCookies.add(new NewCookie("cookie2", "cookie2_value&=?"));
        ParsecAsyncHttpRequest request = builder.setCookies(setCookies).build();

        List<NewCookie> getCookies = new ArrayList<>(request.getCookies());
        assertEquals(2, getCookies.size());
        assertEquals("cookie1", getCookies.get(0).getName());
        assertEquals("cookie1_value", getCookies.get(0).getValue());

    }

    @Test
    public void testSetAndGetFormParams() throws Exception {
        // Test default value
        assertTrue(builder.build().getFormParams().isEmpty());

        // Test add Form param
        Map<String, List<String>> setFormParams = new HashMap<>();
        List<String> key1Values = new ArrayList<>();
        key1Values.add("key1_value1");
        key1Values.add("key1_value2");
        setFormParams.put("key1", key1Values);

        ParsecAsyncHttpRequest request  = builder.setFormParams(setFormParams).build();
        Map<String, List<String>> getFormParams = request.getFormParams();

        assertTrue(getFormParams.containsKey("key1"));
        assertEquals(1, getFormParams.size());
        assertEquals(2, getFormParams.get("key1").size());
        assertEquals("[key1_value1, key1_value2]", getFormParams.get("key1").toString());

        // Test append Form param
        builder.addFormParam("key2", "key2_value1");
        request = builder.build();
        getFormParams = request.getFormParams();

        assertTrue(getFormParams.containsKey("key1"));
        assertTrue(getFormParams.containsKey("key2"));
        assertEquals(2, getFormParams.size());
        assertEquals(2, getFormParams.get("key1").size());
        assertEquals("[key1_value1, key1_value2]", getFormParams.get("key1").toString());

        // Test clear Form params
        assertTrue(builder.setFormParams(null).build().getFormParams().isEmpty());
    }

    @Test
    public void testSetAndGetIsFollowRedirect() throws Exception {
        // Test default value
        assertFalse(builder.build().isFollowRedirect());

        // Test set and get
        assertTrue(builder.setFollowRedirects(true).build().isFollowRedirect());
    }

    @Test
    public void testSetAndGetMaxRetries() throws Exception {
        // Test default value
        assertEquals(3, builder.build().getMaxRetries());

        // Test set and get
        assertEquals(5, builder.setMaxRetries(5).build().getMaxRetries());
    }

    @Test
    public void testSetAndGetProxyServer() throws Exception {
        // Test default value
        assertEquals(null, builder.build().getProxyServer());

        // Test set and get
        ParsecAsyncHttpRequest request = builder
            .setProxyServer(ParsecNetProtocol.HTTPS, "url.for.testing.com", 4443)
            .build();
        assertEquals("https://url.for.testing.com:4443", request.getProxyServer());

        // Test overwrite and get
        request = builder
            .setProxyServer(ParsecNetProtocol.HTTP, "url.for.testing.com", 4080)
            .build();
        assertEquals("http://url.for.testing.com:4080", request.getProxyServer());
    }

    @Test
    public void testSetAndGetQueryParams() throws Exception {
        // Test default value
        assertTrue(builder.build().getQueryParams().isEmpty());

        // Test add query param
        Map<String, List<String>> setQueryParams = new HashMap<>();
        List<String> key1Values = new ArrayList<>();
        key1Values.add("key1_value1");
        key1Values.add("key1_value2");
        setQueryParams.put("key1", key1Values);

        ParsecAsyncHttpRequest request  = builder.setQueryParams(setQueryParams).build();
        Map<String, List<String>> getQueryParams = request.getQueryParams();

        assertTrue(getQueryParams.containsKey("key1"));
        assertEquals(1, getQueryParams.size());
        assertEquals(2, getQueryParams.get("key1").size());
        assertEquals("[key1_value1, key1_value2]", getQueryParams.get("key1").toString());
        assertEquals("http://localhost?key1=key1_value1&key1=key1_value2", request.getUrl());

        // Test append query param
        builder.addQueryParam("key2", "key2_value1");
        request = builder.build();
        getQueryParams = request.getQueryParams();

        assertTrue(getQueryParams.containsKey("key1"));
        assertTrue(getQueryParams.containsKey("key2"));
        assertEquals(2, getQueryParams.size());
        assertEquals(2, getQueryParams.get("key1").size());
        assertEquals("[key1_value1, key1_value2]", getQueryParams.get("key1").toString());
        assertEquals("http://localhost?key1=key1_value1&key1=key1_value2&key2=key2_value1", request.getUrl());
    }

    @Test
    public void testSetAndGetRangeOffSet() throws Exception {
        // Test default value
        assertEquals(0, builder.build().getRangeOffset());

        // Test set and get
        assertEquals(5, builder.setRangeOffset(5).build().getRangeOffset());
    }

    @Test
    public void testSetAndGetRequestTimeout() throws Exception {
        // Test default value
        assertEquals(0, builder.build().getRequestTimeout());

        // Test set and get
        assertEquals(3000, builder.setRequestTimeout(3000).build().getRequestTimeout());
    }

    @Test
    public void testSetAndGetUri() throws Exception {
        // Test default value
        assertEquals("http://localhost", builder.build().getUri().toString());

        // Test set and get
        assertEquals("http://tw.yahoo.com", builder.setUri(
                URI.create("http://tw.yahoo.com")).build().getUri().toString());
    }

    @Test
    public void testSetAndGetUrl() throws Exception {
        // Test default value
        assertEquals("http://localhost", builder.build().getUrl());

        // Test set and get
        assertEquals("http://tw.yahoo.com", builder.setUrl("http://tw.yahoo.com").build().getUri().toString());
    }

    @Test
    public void testSetAndGetVirtualHost() throws Exception {
        // Test default value
        assertEquals(null, builder.build().getVirtualHost());

        // Test set and get
        assertEquals("virtual-host", builder.setVirtualHost("virtual-host").build().getVirtualHost());
    }


    @Test
    public void testSetAndGetAcceptCompression() throws Exception {
        // Test default value
        assertTrue(builder.build().isAcceptCompression());

        // Test default get
        ParsecAsyncHttpRequest request = builder.build();
        assertEquals(request.getHeaderString("accept-encoding"), "deflate");
        assertEquals(request.getHeaderString("Accept-Encoding"), "deflate");
        assertEquals(request.getHeaderString("ACCEPT-ENCODING"), "deflate");

        // Test set disable and get
        assertFalse(builder.setAcceptCompression(false).build().isAcceptCompression());
        ParsecAsyncHttpRequest requestDisable = builder.build();
        assertFalse(requestDisable.getHeaders().containsKey("accept-encoding"));
        assertFalse(requestDisable.getHeaders().containsKey("Accept-Encoding"));
        assertFalse(requestDisable.getHeaders().containsKey("ACCEPT-ENCODING"));

        // Test set enable and get with existing accept-encoding header
        assertTrue(builder.setAcceptCompression(true).build().isAcceptCompression());
        builder.setHeader("accept-encoding", "gzip");
        ParsecAsyncHttpRequest requestExist = builder.build();
        assertEquals(requestExist.getHeaderString("Accept-Encoding"), "gzip");

        // Test set enable and get with existing empty accept-encoding header
        assertTrue(builder.setAcceptCompression(true).build().isAcceptCompression());
        builder.setHeader("accept-encoding", "");
        ParsecAsyncHttpRequest requestExistEmpty = builder.build();
        assertEquals(requestExistEmpty.getHeaderString("Accept-Encoding"), "");
    }

    @Test
    public void testMultipartBody() {
        String filepath = "src/test/java/com/yahoo/parsec/clients/ParsecAsyncHttpRequestTest.java";
        builder.addBodyPart("abc", "def");
        builder.addBodyPart("def", new byte[]{1,2,3}, null);
        builder.addBodyPart("myFile", new File(filepath), null);
        builder.addBodyPart("withContentType", "{\"abc\":\"def\"}", "application/json", StandardCharsets.UTF_8);
        List<Part> parts = builder.build().getNingRequest().getParts();

        assertEquals(parts.get(0).getName(), "abc");
        assertEquals(((StringPart)parts.get(0)).getValue(), "def");
        assertEquals(parts.get(1).getName(), "def");
        assertEquals(((ByteArrayPart)parts.get(1)).getBytes(), new byte[]{1, 2, 3});
        assertEquals(parts.get(2).getName(), "myFile");
        assertEquals(((FilePart)parts.get(2)).getFile().getName(), "ParsecAsyncHttpRequestTest.java");
        assertEquals(parts.get(3).getName(), "withContentType");
        assertEquals(((StringPart)parts.get(3)).getValue(), "{\"abc\":\"def\"}");
    }

    @Test
    public void testQueryParam() throws Exception {
        builder.addQueryParam("encodeParam","+param");
        builder.addQueryParam("normalParam","param");
        ParsecAsyncHttpRequest request  = builder.build();
        Map<String, List<String>> queryParams = request.getQueryParams();
        assertTrue(queryParams.containsKey("encodeParam"));
        assertTrue(queryParams.containsKey("normalParam"));
        assertEquals(2, queryParams.size());
        assertEquals("[%2Bparam]", queryParams.get("encodeParam").toString());
        assertEquals("[param]", queryParams.get("normalParam").toString());
    }

    @Test
    public void testQueryParamFromUri() throws Exception {
        UriBuilder uriBuilder = UriBuilder.fromUri("http://localhost");
        uriBuilder.queryParam("encodeParam", "+param");
        uriBuilder.queryParam("normalParam", "param");
        URI uri = uriBuilder.build();
        builder.setUri(uri);
        ParsecAsyncHttpRequest request  = builder.build();
        Map<String, List<String>> queryParams = request.getQueryParams();
        assertTrue(queryParams.containsKey("encodeParam"));
        assertTrue(queryParams.containsKey("normalParam"));
        assertEquals(2, queryParams.size());
        assertEquals("[%2Bparam]", queryParams.get("encodeParam").toString());
        assertEquals("[param]", queryParams.get("normalParam").toString());
    }

    @Test
    public void testSetAndGetNameResolver() throws Exception {
        // Test default value
        assertSame(builder.build().getNameResolver(), StandardNameResolver.getInstance());

        // Test set and get
        ParsecNameResolver nameResolver = Mockito.mock(ParsecNameResolver.class);
        assertSame(builder.setNameResolver(nameResolver), builder);
        assertSame(builder.build().getNameResolver(), nameResolver);
    }

}
