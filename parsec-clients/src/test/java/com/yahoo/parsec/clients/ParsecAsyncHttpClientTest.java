// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.github.benmanes.caffeine.cache.Cache;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.filter.FilterContext;
import com.ning.http.client.filter.IOExceptionFilter;
import com.ning.http.client.filter.RequestFilter;
import com.ning.http.client.filter.ResponseFilter;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Response;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;


public class ParsecAsyncHttpClientTest {

    private ParsecAsyncHttpClient client;
    private JettyHttpTestServer jettyHttpTestServer;
    private String baseUrl;

    @BeforeTest
    public void setUp() throws Exception {
        client = new ParsecAsyncHttpClient.Builder().build();
        jettyHttpTestServer = new JettyHttpTestServer();
        baseUrl = "http://" + jettyHttpTestServer.getHost() + ":" + jettyHttpTestServer.getPort();
    }

    @AfterTest
    public void tearDown() throws Exception {
        client.close();
        jettyHttpTestServer.stop();
    }

    @Test
    public void testAddGetAndRemoveNingIOExceptionFilter() throws Exception {
        client = new ParsecAsyncHttpClient.Builder().build();
        // Test default value
        List<IOExceptionFilter> ioExceptionFilters = client.getIOExceptionFilters();
        assertEquals(0, ioExceptionFilters.size());

        // Test add
        client = new ParsecAsyncHttpClient.Builder()
            .addIOExceptionFilter(new MockIOExceptionFilter())
            .build();

        ioExceptionFilters = client.getIOExceptionFilters();
        assertEquals(1, ioExceptionFilters.size());
        assertTrue(ioExceptionFilters.get(0) instanceof MockIOExceptionFilter);

        // Test remove
        ParsecAsyncHttpClient.Builder builder = new ParsecAsyncHttpClient.Builder()
            .addIOExceptionFilter(new MockIOExceptionFilter());

        ioExceptionFilters = builder.build().getIOExceptionFilters();
        assertEquals(1, ioExceptionFilters.size());
        ioExceptionFilters = builder
            .removeIOExceptionFilter(ioExceptionFilters.get(0)).build().getIOExceptionFilters();
        assertEquals(0, ioExceptionFilters.size());
    }

    @Test
    public void testAddGetAndRemoveNingRequestFilter() throws Exception {
        client = new ParsecAsyncHttpClient.Builder().build();
        // Test default value
        List<RequestFilter> requestFilters = client.getRequestFilters();
        assertEquals(requestFilters.size(), 0);

        //Test add
        RequestFilter mockRequestFilter = mock(DummyRequestFilter.class);
        client = new ParsecAsyncHttpClient.Builder().enableProfilingFilter(true).addRequestFilter(mockRequestFilter).build();

        int defaultFilterSize = 1;
        requestFilters = client.getRequestFilters();
        assertEquals(requestFilters.size(), defaultFilterSize+1);
        assertTrue(requestFilters.get(0) instanceof DummyRequestFilter);

        //Test remove
        ParsecAsyncHttpClient.Builder builder = new ParsecAsyncHttpClient.Builder().enableProfilingFilter(true)
                .addRequestFilter(mock(RequestFilter.class))
                .addRequestFilter(mockRequestFilter);

        requestFilters = builder.build().getRequestFilters();
        assertEquals(requestFilters.size(), defaultFilterSize+2);
        requestFilters = builder
            .removeRequestFilter(requestFilters.get(0)).build().getRequestFilters();
        assertEquals(requestFilters.size(), defaultFilterSize+1);
        assertTrue(requestFilters.get(0) instanceof DummyRequestFilter);
    }

    @Test
    public void testAddGetAndRemoveNingResponseFilter() throws Exception {
        // Test default value
        List<ResponseFilter> responseFilters = client.getResponseFilters();
        assertEquals(0, responseFilters.size());

        // Test add
        client = new ParsecAsyncHttpClient.Builder()
            .addResponseFilter(new MockResponseFilter())
            .build();

        responseFilters = client.getResponseFilters();
        assertEquals(1, responseFilters.size());
        assertTrue(responseFilters.get(0) instanceof MockResponseFilter);

        // Test remove
        ParsecAsyncHttpClient.Builder builder = new ParsecAsyncHttpClient.Builder()
            .addResponseFilter(new MockResponseFilter());

        responseFilters = builder.build().getResponseFilters();
        assertEquals(1, responseFilters.size());
        responseFilters = builder
            .removeResponseFilter(responseFilters.get(0)).build().getResponseFilters();
        assertEquals(0, responseFilters.size());
    }

    @Test
    public void testBuilderConstructorNingAsyncHttpClientConfig() throws Exception {
        client = new ParsecAsyncHttpClient.Builder(new AsyncHttpClientConfig.Builder().build()).build();
        assertNotNull(client);
    }

    @Test
    public void testBuilderConstructorNingAsyncHttpClientConfigBuilder() throws Exception {
        client = new ParsecAsyncHttpClient.Builder(new AsyncHttpClientConfig.Builder()).build();
        assertNotNull(client);
    }

    @Test
    public void testBuilderListExec() throws Exception {
        List<ParsecAsyncHttpRequest> builders = new ArrayList<>();

        builders.add(new ParsecAsyncHttpRequest.Builder()
            .setUrl(baseUrl + "/200")
            .build());

        builders.add(new ParsecAsyncHttpRequest.Builder()
            .setUrl(baseUrl + "/200")
            .build());

        List<CompletableFuture<Response>> futures = client.execute(builders);
        assertEquals(2, futures.size());
    }

    @Test
    public void testBuilderListExecWithMixedRetry() throws Exception {
        List<ParsecAsyncHttpRequest> builders = new ArrayList<>();

        builders.add(new ParsecAsyncHttpRequest.Builder()
            .setUrl(baseUrl + "/200")
            .build());

        builders.add(new ParsecAsyncHttpRequest.Builder()
            .setUrl(baseUrl + "/200")
            .addRetryStatusCode(500)
            .build());

        List<CompletableFuture<Response>> futures = client.execute(builders);
        assertEquals(2, futures.size());
    }

    @Test
    public void testBuilderListExecWithRetry() throws Exception {
        List<ParsecAsyncHttpRequest> builders = new ArrayList<>();

        builders.add(new ParsecAsyncHttpRequest.Builder()
            .setUrl(baseUrl + "/200")
            .addRetryStatusCode(500)
            .build());

        builders.add(new ParsecAsyncHttpRequest.Builder()
            .setUrl(baseUrl + "/200")
            .addRetryStatusCode(500)
            .build());

        List<CompletableFuture<Response>> futures = client.execute(builders);
        assertEquals(2, futures.size());
    }

    @Test
    public void testClose() throws Exception {
        ParsecAsyncHttpClient client = new ParsecAsyncHttpClient.Builder()
            .setExecutorService(Executors.newFixedThreadPool(1))
            .build();
        assertFalse(client.isClosed());
        assertFalse(client.getExecutorService().isShutdown());

        client.close();

        assertTrue(client.isClosed());
        assertTrue(client.getExecutorService().isShutdown());
    }

    @Test
    public void testGetNingClientConfig() throws Exception {
        assertNotNull(client.getNingClientConfig());
    }

    @Test
    public void testGetSharedParsecExecutorService() {
        assertNotNull(client.getExecutorService());
    }

    @Test
    public void testPackageLevelConstructor() throws Exception {
        Constructor constructor = ParsecAsyncHttpClient.class.getDeclaredConstructor();
        assertEquals(constructor.getModifiers(), 0);
        constructor.newInstance();
    }

    @Test
    public void testSetAcceptAnyCertificate() throws Exception {
        // Test default value
        assertFalse(client.isAcceptAnyCertificate());

        // Test set and get
        client = new ParsecAsyncHttpClient.Builder().setAcceptAnyCertificate(true).build();
        assertTrue(client.isAcceptAnyCertificate());
    }

    @Test
    public void testSetAndGetConnectTimeout() throws Exception {
        // Test default value
        assertEquals(5000, client.getConnectTimeout());

        // Test set and get
        client = new ParsecAsyncHttpClient.Builder().setConnectTimeout(3000).build();
        assertEquals(3000, client.getConnectTimeout());
    }

    @Test
    public void testSetAndGetIsAllowPoolingConnections() throws Exception {
        // Test default value
        assertEquals(true, client.isAllowPoolingConnections());

        // Test set and get
        client = new ParsecAsyncHttpClient.Builder().setAllowPoolingConnections(false).build();
        assertEquals(false, client.isAllowPoolingConnections());
    }

    @Test
    public void testSetAndGetIsAllowPoolingSslConnections() throws Exception {
        // Test default value
        assertEquals(true, client.isAllowPoolingSslConnections());

        // Test set and get
        client = new ParsecAsyncHttpClient.Builder().setAllowPoolingSslConnections(false).build();
        assertEquals(false, client.isAllowPoolingSslConnections());
    }

    @Test
    public void testSetAndGetIsCompressionEnforced() throws Exception {
        // Test default value
        assertEquals(false, client.isCompressionEnforced());

        // Test set and get
        client = new ParsecAsyncHttpClient.Builder().setCompressionEnforced(true).build();
        assertEquals(true, client.isCompressionEnforced());
    }

    @Test
    public void testSetAndGetMaxConnections() throws Exception {
        // Test default value
        assertEquals(-1, client.getMaxConnections());

        // Test set and get
        client = new ParsecAsyncHttpClient.Builder().setMaxConnections(256).build();
        assertEquals(256, client.getMaxConnections());
    }

    @Test
    public void testSetAndGetMaxConnectionsPerHost() throws Exception {
        // Test default value
        assertEquals(-1, client.getMaxConnectionsPerHost());

        // Test set and get
        client = new ParsecAsyncHttpClient.Builder().setMaxConnectionsPerHost(128).build();
        assertEquals(128, client.getMaxConnectionsPerHost());
    }

    @Test
    public void testSetAndGetMaxRedirects() throws Exception {
        // Test default value
        assertEquals(5, client.getMaxRedirects());

        // Test set and get
        client = new ParsecAsyncHttpClient.Builder().setMaxRedirects(3).build();
        assertEquals(3, client.getMaxRedirects());
    }

    @Test
    public void testSetAndGetPooledConnectionIdleTimeout() throws Exception {
        // Test default value
        assertEquals(60000, client.getPooledConnectionIdleTimeout());

        // Test set and get
        client = new ParsecAsyncHttpClient.Builder().setPooledConnectionIdleTimeout(15000).build();
        assertEquals(15000, client.getPooledConnectionIdleTimeout());
    }

    @Test
    public void testSetAndGetReadTimeout() throws Exception {
        // Test default value
        assertEquals(60000, client.getReadTimeout());

        // Test set and get
        client = new ParsecAsyncHttpClient.Builder().setReadTimeout(30000).build();
        assertEquals(30000, client.getReadTimeout());
    }

    @Test
    public void testSetAndGetRequestTimeout() throws Exception {
        // Test default value
        assertEquals(60000, client.getRequestTimeout());

        // Test set and get
        client = new ParsecAsyncHttpClient.Builder().setRequestTimeout(30000).build();
        assertEquals(30000, client.getRequestTimeout());
    }

    @Test
    public void testSetAndGetUserAgent() throws Exception {

        // Test set and get
        client = new ParsecAsyncHttpClient.Builder().setUserAgent("UserFOR/1.0").build();
        assertEquals("UserFOR/1.0", client.getUserAgent());
    }

    @Test
    public void testSetAndGetSSLContext() throws NoSuchAlgorithmException {
        SSLContext mockSslContext = mock(SSLContext.class);
        client = new ParsecAsyncHttpClient.Builder().setSSLContext(mockSslContext).build();

        assertEquals(client.getSSLContext(), mockSslContext);
    }

    @Test
    public void testSetAngGetIsFollowRedirect() throws Exception {
        // Test default value
        assertEquals(false, client.isFollowRedirect());

        // Test set and get
        client = new ParsecAsyncHttpClient.Builder().setFollowRedirect(true).build();
        assertEquals(true, client.isFollowRedirect());
    }

    @Test
    public void testSetConnectionTTL() throws Exception {
        // Test default value
        assertEquals(-1, client.getConnectionTTL());

        // Test set and get
        client = new ParsecAsyncHttpClient.Builder().setConnectionTTL(60).build();
        assertEquals(60, client.getConnectionTTL());
    }

    @Test
    public void testSetMaxRequestRetry() throws Exception {
        // Test default value
        assertEquals(5, client.getMaxRequestRetry());

        // Test set and get
        client = new ParsecAsyncHttpClient.Builder().setMaxRequestRetry(3).build();
        assertEquals(3, client.getMaxRequestRetry());
    }

    @Test
    public void testSharedParsecExecutorService() throws Exception {
        client = new ParsecAsyncHttpClient.Builder().build();
        ThreadPoolExecutor executor = client.getExecutorService();

        assertNotNull(executor);
    }

    @Test
    public void testSingleRequestExec() throws Exception {
        ParsecAsyncHttpRequest request = new ParsecAsyncHttpRequest.Builder()
                .setUrl(baseUrl + "/200")
                .build();

        Response response = client.execute(request).get();
        assertNotNull(response.getHeaderString(ParsecClientDefine.HEADER_HOST));
    }

    @Test
    public void testSingleRequestExecWithRetrySettings() throws Exception {
        ParsecAsyncHttpRequest request = new ParsecAsyncHttpRequest.Builder()
                .setUrl(baseUrl + "/200")
                .addRetryStatusCode(500)
                .addRetryException(TimeoutException.class)
                .setMaxRetries(2)
                .build();

        Response response = client.execute(request).get();
        assertNotNull(response.getHeaderString(ParsecClientDefine.HEADER_HOST));
    }

    @Test
    public void testMultipleRequestsExec() throws Exception {

        ParsecAsyncHttpRequest request1 = new ParsecAsyncHttpRequest.Builder()
                .setCriticalGet(true)
                .setUrl(baseUrl + "/200")
                .build();
        ParsecAsyncHttpRequest request2 = new ParsecAsyncHttpRequest.Builder()
                .setCriticalGet(true)
                .setUrl(baseUrl + "/200")
                .build();
        ParsecAsyncHttpRequest request3 = new ParsecAsyncHttpRequest.Builder()
                .setCriticalGet(true)
                .setUrl(baseUrl + "/200")
                .build();

        Response response1 = client.execute(request1).get();
        Response response2 = client.execute(request2).get();
        Response response3 = client.execute(request3).get();

        assertNotNull(response1.getHeaderString(ParsecClientDefine.HEADER_HOST));
        assertNotNull(response2.getHeaderString(ParsecClientDefine.HEADER_HOST));
        assertNotNull(response3.getHeaderString(ParsecClientDefine.HEADER_HOST));
    }

    @Test
    public void testRequestListExec() throws Exception {

        ParsecAsyncHttpRequest request = new ParsecAsyncHttpRequest.Builder()
                .setCriticalGet(true)
                .setUrl(baseUrl + "/200")
                .build();
        ParsecAsyncHttpRequest request2 = new ParsecAsyncHttpRequest.Builder()
                .setCriticalGet(true)
                .setUrl(baseUrl + "/200")
                .build();
        ParsecAsyncHttpRequest request3 = new ParsecAsyncHttpRequest.Builder()
                .setCriticalGet(true)
                .setUrl(baseUrl + "/200")
                .build();

        List<CompletableFuture<Response>> futures =
            client.execute(Arrays.asList(request, request2, request3));

        for (CompletableFuture<Response> future: futures) {
            Response response = future.get();
            assertNotNull(response.getHeaderString(ParsecClientDefine.HEADER_HOST));
        }
    }

    @Test
    public void testSingleRequestExecWithRetryOnStatusCode() throws Exception {
        ParsecAsyncHttpRequest request = new ParsecAsyncHttpRequest.Builder()
            .setCriticalGet(true)
            .setUrl(baseUrl + "/500")
            .addRetryStatusCode(500)
            .setMaxRetries(2)
            .build();

        Response response = client.execute(request).get();
        assertNotNull(response.getHeaderString(ParsecClientDefine.HEADER_HOST));
    }

    @Test(expectedExceptions = ExecutionException.class, expectedExceptionsMessageRegExp = "java.util.concurrent.TimeoutException.*")
    public void testTimeoutShouldThrowTimeoutException() throws Exception {
        ParsecAsyncHttpRequest request = new ParsecAsyncHttpRequest.Builder()
                .setCriticalGet(true)
                .setUrl(baseUrl + "/sleep/300")
                .setRequestTimeout(10)
                .build();

        client.execute(request).get();
    }

    @Test
    public void testSingleRequestExecWithRetryOnException() throws Exception {
        ParsecAsyncHttpRequest request = new ParsecAsyncHttpRequest.Builder()
            .setCriticalGet(true)
            .setUrl(baseUrl + "/sleep/25")
            .addRetryException(TimeoutException.class)
            .setRequestTimeout(10)
            .setMaxRetries(2)
            .build();

        Response response = client.execute(request).get();
        assertNotNull(response.getHeaderString(ParsecClientDefine.HEADER_HOST));
    }

    @Test
    public void testSettingRetryIntervalWillAffectRetryBehavior() throws Exception {
        ThreadPoolExecutor executorService = mock(ThreadPoolExecutor.class);
        AsyncHttpClientConfig clientConfig = mock(AsyncHttpClientConfig.class);
        when(clientConfig.executorService()).thenReturn(executorService);
        AsyncHttpClientConfig.Builder configBuilder = mock(AsyncHttpClientConfig.Builder.class);
        when(configBuilder.build()).thenReturn(clientConfig);

        client = new ParsecAsyncHttpClient.Builder(configBuilder)
            .setRetryIntervalInMilliSeconds(100)
            .build();

        ParsecAsyncHttpRequest request = new ParsecAsyncHttpRequest.Builder()
            .setCriticalGet(true)
            .setUrl(baseUrl + "/sleep/25")
            .addRetryException(TimeoutException.class)
            .setRequestTimeout(10)
            .setMaxRetries(2)
            .build();

        CompletableFuture completableFuture = client.execute(request);
        assertNotNull(completableFuture);
        assertTrue(completableFuture instanceof ParsecCompletableFuture);

        ArgumentCaptor<ParsecHttpRequestRetryCallable> argumentCaptor = ArgumentCaptor.forClass(ParsecHttpRequestRetryCallable.class);
        verify(executorService, times(1)).submit(argumentCaptor.capture());
        ParsecHttpRequestRetryCallable retryCallable = argumentCaptor.getValue();
        assertEquals(100, retryCallable.getRetryDelayer().getRetryIntervalMillis());
    }

    @Test
    public void testSettingRetryIntervalCanSuccessfulRetry() throws Exception {
        ParsecAsyncHttpRequest request = new ParsecAsyncHttpRequest.Builder()
            .setCriticalGet(true)
            .setUrl(baseUrl + "/500")
            .addRetryStatusCode(500)
            .setMaxRetries(2)
            .build();

        client = new ParsecAsyncHttpClient.Builder()
            .setRetryIntervalInMilliSeconds(100)
            .build();

        Response response = client.execute(request).get();
        assertNotNull(response.getHeaderString(ParsecClientDefine.HEADER_HOST));
    }

    //@Test
    public void testMultiPart() throws Exception {
        CompletableFuture<Response> future = client.execute(new ParsecAsyncHttpRequest.Builder()
                .addBodyPart("key1", "val1")
                .addBodyPart("key2", "val2", "text/plain", StandardCharsets.UTF_8)
                .setUrl(baseUrl + "/200")
                .setMethod("POST")
                .build());
        Response response = future.get();
        assertEquals(response.getHeaderString("key1"), "val1");
        assertEquals(response.getHeaderString("key2"), "val2");
    }

    @SuppressWarnings("unchecked")
    private class MockResponseFilter implements ResponseFilter {
        public FilterContext filter(FilterContext ctx) {
            return null;
        }
    }

    private class MockIOExceptionFilter implements IOExceptionFilter {
        public FilterContext filter(FilterContext ctx) {
            return null;
        }
    }
    private class DummyRequestFilter implements RequestFilter{
        public <T> FilterContext<T> filter(FilterContext<T> ctx) { return null; }
    }

    @Test
    public void testThenApply() throws Exception {
        Long start = System.currentTimeMillis();

        CompletableFuture<Response> future1 = client.execute(new ParsecAsyncHttpRequest.Builder()
            .setUrl(baseUrl + "/200")
            .build());

        CompletableFuture<Long> future2 = future1.thenApply(response -> {
            try {
                Thread.sleep(100);
                return System.currentTimeMillis();
            } catch (InterruptedException e) {
                throw  new RuntimeException(e);
            }
        });

        assertTrue(System.currentTimeMillis() - start <= 150);
        assertTrue(future2.get() - start >= 100);
    }

    @Test
    public void testEnableRecordStatsShouldReturnStatsData() throws Exception {
        client = new ParsecAsyncHttpClient.Builder().recordCacheStats().build();
        ParsecAsyncHttpRequest request = new ParsecAsyncHttpRequest.Builder().setUrl(baseUrl + "/200").build();
        client.execute(request).get();
        client.execute(request).get();

        ParsecAsyncHttpClient.CacheStats cacheStats = client.getCacheStats();
        assertEquals(cacheStats.requestCount(), 2);
        assertEquals(cacheStats.hitCount(), 1);
        assertEquals(cacheStats.missCount(), 1);
    }
}
