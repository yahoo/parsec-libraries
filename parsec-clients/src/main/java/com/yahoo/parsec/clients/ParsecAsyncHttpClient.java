// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Request;
import com.ning.http.client.filter.IOExceptionFilter;
import com.ning.http.client.filter.RequestFilter;
import com.ning.http.client.filter.ResponseFilter;
import com.yahoo.parsec.filters.AlwaysTruePredicate;
import com.yahoo.parsec.filters.NingRequestResponseFormatter;
import com.yahoo.parsec.filters.ProfilingFilter;
import com.yahoo.parsec.filters.ProfilingFormatter;
import com.yahoo.parsec.filters.ResponseOrThrowable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;


/**
 * HTTP clients that supports asynchronous and synchronous requests.
 *
 * @author sho
 */
@SuppressWarnings("unused")
public class ParsecAsyncHttpClient {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ParsecAsyncHttpClient.class);

    /**
     * Client.
     */
    private AsyncHttpClient client;

    /**
     * Retry interval
     */
    private Optional<Long> retryIntervalMillisOpt;

    /**
     * Ning client config.
     */
    private AsyncHttpClientConfig ningClientConfig;

    /**
     * Response loading cache.
     */
    private ParsecAsyncHttpResponseLoadingCache responseLoadingCache;

    /**
     * Executor.
     */
    private ThreadPoolExecutor executorService;

    private static final BiPredicate<Request, ResponseOrThrowable> PROFILE_LOGGING_PREDICATE
            = new AlwaysTruePredicate();

    private static final NingRequestResponseFormatter PROFILE_LOGGING_FORMATTER = new ProfilingFormatter();
    private static final String PROFILE_LOGGING_LOG_NAME = "parsec.clients.profiling_log";

    private static final RequestFilter PROFILING_FILTER = new ProfilingFilter(PROFILE_LOGGING_PREDICATE,
                                                                      PROFILE_LOGGING_FORMATTER,
                                                                      PROFILE_LOGGING_LOG_NAME);


    //for internal use only for now so it's easier to switch back-and-forth for debugging , use the wrapper for profiling
    private boolean oldFashionProfiling = true;

    /**
     * Unused constructor.
     */
    ParsecAsyncHttpClient() {

    }

    /**
     * Private constructor.
     * @param builder builder
     */
    private ParsecAsyncHttpClient(final Builder builder) {
        this(builder.configBuilder,
                builder.cacheRefreshAfterWrite,
                builder.cacheExpireAfterWrite,
                builder.cacheMaximumSize,
                builder.enableProfilingFilter,
                builder.recordCacheStats,
                builder.retryIntervalMillisOpt);
    }

    /**
     * Private constructor.
     * @param ningClientConfigBuilder Ning client config builder
     * @param cacheExpireAfterWrite cache expire time
     * @param cacheMaximumSize cache maximum size
     */
    private ParsecAsyncHttpClient(
        final AsyncHttpClientConfig.Builder ningClientConfigBuilder,
        int cacheRefreshAfterWrite,
        int cacheExpireAfterWrite,
        int cacheMaximumSize,
        boolean enableProfilingFilter,
        boolean recordCacheStats,
        Optional<Long> retryIntervalMillisOpt
    ) {
        ParsecAsyncHttpResponseLoadingCache.Builder cacheBuilder = new ParsecAsyncHttpResponseLoadingCache.Builder(this)
                .expireAfterWrite(cacheExpireAfterWrite, TimeUnit.SECONDS)
                .maximumSize(cacheMaximumSize);

        if(cacheRefreshAfterWrite > 0)
            cacheBuilder.refreshAfterWrite(cacheRefreshAfterWrite, TimeUnit.SECONDS);

        if (recordCacheStats) {
            cacheBuilder.recordStats();
        }

        responseLoadingCache = cacheBuilder.build();

        if (enableProfilingFilter) {
            //so that there's only one filter.
            ningClientConfigBuilder.removeRequestFilter(PROFILING_FILTER);
            ningClientConfigBuilder.addRequestFilter(PROFILING_FILTER);
            oldFashionProfiling = false;
        }

        this.ningClientConfig = ningClientConfigBuilder.build();

        executorService = (ThreadPoolExecutor) ningClientConfig.executorService();
        client = new AsyncHttpClient(ningClientConfig);

        this.retryIntervalMillisOpt = retryIntervalMillisOpt;
    }

    /**
     * Close the under-lying connections and shut down executor service.
     */
    public void close() {
        if (!client.isClosed()) {
            client.close();
        }

        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }

        responseLoadingCache.shutdownCleanUpExecutorService();
    }

    /**
     * Critical execute requests (will not lookup in cache nor load into cache).
     *
     * @param requests Requests to critical execute
     * @return A list of {@literal CompletableFuture<Response>}
     * @throws ExecutionException execution exception
     */
    public List<CompletableFuture<Response>> criticalExecute(
        final List<ParsecAsyncHttpRequest> requests) throws ExecutionException {
        List<CompletableFuture<Response>> futures = new ArrayList<>();

        for (ParsecAsyncHttpRequest request : requests) {
            futures.add(criticalExecute(request));
        }

        return futures;
    }

    /**
     * Critical execute a request (will not lookup in cache nor load into cache).
     *
     * @param request Request to critical execute
     * @return {@literal CompletableFuture<Response>}
     * @throws ExecutionException execution exception
     */
    public CompletableFuture<Response> criticalExecute(final ParsecAsyncHttpRequest request) throws ExecutionException {
        return criticalExecute(request, new ParsecAsyncCompletionHandlerBase());
    }

    /**
     * Critical execute a request (will not lookup in cache nor load into cache).
     *
     * @param request Request to critical execute
     * @param asyncHandler Request async handler
     * @param <T> Response type
     * @return {@literal CompletableFuture<T>}
     */
    public <T> CompletableFuture<T> criticalExecute(
        final ParsecAsyncHttpRequest request,
        AsyncHandler<T> asyncHandler
    ) {
        AsyncHandler<T> practicalAsyncHandler =
            oldFashionProfiling? new ParsecAsyncHandlerWrapper<>(asyncHandler, request.getNingRequest()): asyncHandler;

        if (request.getRetryStatusCodes().isEmpty() && request.getRetryExceptions().isEmpty()) {
            return new ParsecCompletableFuture<>(
                client.executeRequest(request.getNingRequest(), practicalAsyncHandler)
            );
        } else {
            ParsecHttpRequestRetryCallable<T> retryCallable;
            retryCallable = retryIntervalMillisOpt.map(retryIntervalMillis -> new ParsecHttpRequestRetryCallable<>(
                client, request, practicalAsyncHandler, retryIntervalMillis))
                .orElseGet(() -> new ParsecHttpRequestRetryCallable<>(client, request, practicalAsyncHandler));
            return new ParsecCompletableFuture<>(executorService.submit(retryCallable));
        }
    }

    /**
     * Execute requests.
     *
     * @param requests Requests to execute
     * @return A list of {@literal CompletableFuture<Response>}
     * @throws ExecutionException execution exception
     */
    public List<CompletableFuture<Response>> execute(
        final List<ParsecAsyncHttpRequest> requests
    ) throws ExecutionException {
        List<CompletableFuture<Response>> futures = new ArrayList<>();

        for (ParsecAsyncHttpRequest request : requests) {
            futures.add(execute(request));
        }

        return futures;
    }

    /**
     * Execute request.
     * @param request Request to execute
     * @return {@literal CompletableFuture<Response>}
     * @throws ExecutionException execution exception
     */
    public CompletableFuture<Response> execute(final ParsecAsyncHttpRequest request) throws ExecutionException {
        if (!request.isCriticalGet() && request.getMethod().equals("GET")) {
            return responseLoadingCache.get(request, executorService);
        }

        return criticalExecute(request);
    }

    /**
     * Get cache statistics
     *
     * @return cache statistics
     */
    public CacheStats getCacheStats() {
        return new CacheStats(responseLoadingCache.synchronous().stats());
    }

    /**
     * Get connection timeout.
     *
     * @return Connection timeout
     */
    public int getConnectTimeout() {
        return ningClientConfig.getConnectTimeout();
    }

    /**
     * Get connection TTL.
     *
     * @return Connection TTL
     */
    public int getConnectionTTL() {
        return ningClientConfig.getConnectionTTL();
    }

    /**
     * Get executor service.
     *
     * @return Executor service
     */
    ThreadPoolExecutor getExecutorService() {
        return executorService;
    }

    /**
     * Get IO exception filters.
     *
     * @return {@link IOExceptionFilter} {@link List}
     */
    public List<IOExceptionFilter> getIOExceptionFilters() {
        return ningClientConfig.getIOExceptionFilters();
    }

    /**
     * Get max connections.
     *
     * @return Max conections
     */
    public int getMaxConnections() {
        return ningClientConfig.getMaxConnections();
    }

    /**
     * Get max connections per host.
     *
     * @return Max connections per host
     */
    public int getMaxConnectionsPerHost() {
        return ningClientConfig.getMaxConnectionsPerHost();
    }

    /**
     * Get max redirects.
     *
     * @return Max redirects
     */
    public int getMaxRedirects() {
        return ningClientConfig.getMaxRedirects();
    }

    /**
     * Get max request retry.
     *
     * @return Max request retry
     */
    public int getMaxRequestRetry() {
        return ningClientConfig.getMaxRequestRetry();
    }

    /**
     * Get Ning {@link AsyncHttpClientConfig}.
     *
     * @return Ning {@link AsyncHttpClientConfig}
     */
    AsyncHttpClientConfig getNingClientConfig() {
        return ningClientConfig;
    }

    /**
     * Get pooled connection idle timeout.
     *
     * @return Pooled connection idle timeout
     */
    public int getPooledConnectionIdleTimeout() {
        return ningClientConfig.getPooledConnectionIdleTimeout();
    }

    /**
     * Get read timeout.
     *
     * @return Read timeout
     */
    public int getReadTimeout() {
        return ningClientConfig.getReadTimeout();
    }

    /**
     * Get request filters.
     *
     * @return {@link RequestFilter} {@link List}
     */
    public List<RequestFilter> getRequestFilters() {
        return ningClientConfig.getRequestFilters();
    }

    /**
     * Get request timeout.
     *
     * @return Request timeout
     */
    public int getRequestTimeout() {
        return ningClientConfig.getRequestTimeout();
    }

    /**
     * Get response filters.
     *
     * @return {@link ResponseFilter} {@link List}
     */
    public List<ResponseFilter> getResponseFilters() {
        return ningClientConfig.getResponseFilters();
    }

    /**
     * Get user agent.
     *
     * @return User agent
     */
    public String getUserAgent() {
        return ningClientConfig.getUserAgent();
    }

    /**
     * Get SSL context.
     * @return SSL Context
     */
    public SSLContext getSSLContext() {
        return ningClientConfig.getSSLContext();
    }

    /**
     * Is accept any certificate.
     *
     * @return Whether to accept any certificate
     */
    public boolean isAcceptAnyCertificate() {
        return ningClientConfig.isAcceptAnyCertificate();
    }

    /**
     * Is allow pooling connections.
     *
     * @return Whether to allow pooling connections
     */
    public boolean isAllowPoolingConnections() {
        return ningClientConfig.isAllowPoolingConnections();
    }
    /**
     * Is allow pooling SSL connections.
     *
     * @return Whether to allow pooling SSL connections
     */
    public boolean isAllowPoolingSslConnections() {
        return ningClientConfig.isAllowPoolingSslConnections();
    }

    /**
     * Is closed.
     *
     * @return Whether clients is closed
     */
    public boolean isClosed() {
        return client.isClosed();
    }

    /**
     * Is compression enforced.
     *
     * @return Whether compression is enforced
     */
    public boolean isCompressionEnforced() {
        return ningClientConfig.isCompressionEnforced();
    }

    /**
     * Is follow redirect.
     *
     * @return Whether to follow redirect
     */
    public boolean isFollowRedirect() {
        return ningClientConfig.isFollowRedirect();
    }


    /**
     * Static Builder class for {@link ParsecAsyncHttpClient}.
     *
     * @author sho
     */
    public static class Builder {
        /**
         * Default cache max size.
         */
        private static final int DEFAULT_CACHE_MAX_SIZE = 10000;

        /**
         * Config builder.
         */
        private AsyncHttpClientConfig.Builder configBuilder;

        /**
         * Cache expire time.
         */
        private int cacheExpireAfterWrite = 2;

        /**
         * Cache refresh time.
         */
        private int cacheRefreshAfterWrite = -1;

        /**
         * Cache max size.
         */
        private int cacheMaximumSize = DEFAULT_CACHE_MAX_SIZE;

        /**
         * Retry interval
         */
        private Optional<Long> retryIntervalMillisOpt = Optional.empty();

        private boolean enableProfilingFilter = false;

        private boolean recordCacheStats = false;

        /**
         * Constructor.
         */
        public Builder() {
            this(new AsyncHttpClientConfig.Builder());
        }

        /**
         * Constructor.
         * @param config config
         */
        public Builder(final AsyncHttpClientConfig config) {
            this(new AsyncHttpClientConfig.Builder(config));
        }

        /**
         * Constructor.
         * @param configBuilder config builder
         */
        public Builder(final AsyncHttpClientConfig.Builder configBuilder) {
            this.configBuilder = configBuilder;
        }

        @Override
        public boolean equals(Object object) {
            return EqualsBuilder.reflectionEquals(this, object);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }


        /**
         * Add IO exception filter.
         *
         * @param ioExceptionFilter {@link IOExceptionFilter}
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder addIOExceptionFilter(IOExceptionFilter ioExceptionFilter) {
            configBuilder.addIOExceptionFilter(ioExceptionFilter);
            return this;
        }

        /**
         * Add request filter.
         *
         * @param requestFilter {@link RequestFilter}
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder addRequestFilter(RequestFilter requestFilter) {
            configBuilder.addRequestFilter(requestFilter);
            return this;
        }

        /**
         * Add response filter.
         *
         * @param responseFilter {@link ResponseFilter}
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder addResponseFilter(ResponseFilter responseFilter) {
            configBuilder.addResponseFilter(responseFilter);
            return this;
        }

        public Builder enableProfilingFilter(boolean enable){
            enableProfilingFilter = enable;
            return this;
        }

        public boolean isProfilingFilterEnabled() {
            return enableProfilingFilter;
        }

        public Builder recordCacheStats(){
            recordCacheStats = true;
            return this;
        }

        /**
         * Build new {@link ParsecAsyncHttpClient} instance.
         *
         * @return {@link ParsecAsyncHttpClient}
         */
        public ParsecAsyncHttpClient build() {
            return new ParsecAsyncHttpClient(this);
        }

        /**
         * Remove IO exception filter.
         *
         * @param ioExceptionFilter {@link IOExceptionFilter}
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder removeIOExceptionFilter(IOExceptionFilter ioExceptionFilter) {
            configBuilder.removeIOExceptionFilter(ioExceptionFilter);
            return this;
        }

        /**
         * Remove request filter.
         *
         * @param requestFilter {@link RequestFilter}
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder removeRequestFilter(RequestFilter requestFilter) {
            configBuilder.removeRequestFilter(requestFilter);
            return this;
        }

        /**
         * Remove response filter.
         *
         * @param responseFilter {@link ResponseFilter}
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder removeResponseFilter(ResponseFilter responseFilter) {
            configBuilder.removeResponseFilter(responseFilter);
            return this;
        }

        /**
         * Set accept any certificate.
         *
         * @param acceptAnyCertificate Accept any certificate
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setAcceptAnyCertificate(boolean acceptAnyCertificate) {
            configBuilder.setAcceptAnyCertificate(acceptAnyCertificate);
            return this;
        }

        /**
         * Set allow pooling connections.
         *
         * @param allowPoolingConnections Allow pooling connections
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setAllowPoolingConnections(boolean allowPoolingConnections) {
            configBuilder.setAllowPoolingConnections(allowPoolingConnections);
            return this;
        }

        /**
         * Set allow pooling SSL connections.
         *
         * @param allowPoolingSslConnections Allow pooling SSL connections
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setAllowPoolingSslConnections(boolean allowPoolingSslConnections) {
            configBuilder.setAllowPoolingSslConnections(allowPoolingSslConnections);
            return this;
        }

        /**
         * Set compression enforced.
         * @param compressionEnforced Compression enforced
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setCompressionEnforced(boolean compressionEnforced) {
            configBuilder.setCompressionEnforced(compressionEnforced);
            return this;
        }

        /**
         * Set conection timeout.
         *
         * @param connectTimeOut Connection timeout
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setConnectTimeout(int connectTimeOut) {
            configBuilder.setConnectTimeout(connectTimeOut);
            return this;
        }

        /**
         * Set Connection TTL.
         *
         * @param connectionTTL Connection TTL
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setConnectionTTL(int connectionTTL) {
            configBuilder.setConnectionTTL(connectionTTL);
            return this;
        }

        /**
         * Set executor service for executing requests, handling retries, and asynchronous cache loading.
         *
         * @param executorService Executor service
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setExecutorService(ExecutorService executorService) {
            configBuilder.setExecutorService(executorService);
            return this;
        }

        /**
         * Set follow redirect.
         *
         * @param followRedirect Follow redirect
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setFollowRedirect(boolean followRedirect) {
            configBuilder.setFollowRedirect(followRedirect);
            return this;
        }

        /**
         * Set max connections.
         *
         * @param maxConnections Max connections
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setMaxConnections(int maxConnections) {
            configBuilder.setMaxConnections(maxConnections);
            return this;
        }

        /**
         * Set max connections per host.
         *
         * @param maxConnectionsPerHost Max connections per host
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setMaxConnectionsPerHost(int maxConnectionsPerHost) {
            configBuilder.setMaxConnectionsPerHost(maxConnectionsPerHost);
            return this;
        }

        /**
         * Set max redirect.

         * @param maxRedirects Max redirects
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setMaxRedirects(int maxRedirects) {
            configBuilder.setMaxRedirects(maxRedirects);
            return this;
        }

        /**
         * Set max request retry.
         *
         * @param maxRequestRetry Max request retry
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setMaxRequestRetry(int maxRequestRetry) {
            configBuilder.setMaxRequestRetry(maxRequestRetry);
            return this;
        }

        /**
         * Set pooled connection idle timeout.
         *
         * @param pooledConnectionIdleTimeout Pooled connection idle timeout
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setPooledConnectionIdleTimeout(int pooledConnectionIdleTimeout) {
            configBuilder.setPooledConnectionIdleTimeout(pooledConnectionIdleTimeout);
            return this;
        }

        /**
         * Set read timeout.
         *
         * @param readTimeout Read timeout
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setReadTimeout(int readTimeout) {
            configBuilder.setReadTimeout(readTimeout);
            return this;
        }

        /**
         * Set request timeout.
         *
         * @param requestTimeout Request timeout
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setRequestTimeout(int requestTimeout) {
            configBuilder.setRequestTimeout(requestTimeout);
            return this;
        }

        /**
         * Set user agent.
         *
         * @param userAgent User agent
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setUserAgent(String userAgent) {
            configBuilder.setUserAgent(userAgent);
            return this;
        }

        /**
         * Set ssl context.
         * @param sslContext SSL context
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setSSLContext(SSLContext sslContext) {
            configBuilder.setSSLContext(sslContext);
            return this;
        }

        /**
         * Set cache expire after write.
         * @param cacheExpireAfterWrite How long before cache expires after write
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setCacheExpireAfterWrite(int cacheExpireAfterWrite) {
            this.cacheExpireAfterWrite = cacheExpireAfterWrite;
            return this;
        }

        /**
         * Set whether cache need auto refresh mechanism. 
         * @param cacheRefreshAfterWrite How long will a key become eligible for refresh after write
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setCacheNeedAsyncRefresh(int cacheRefreshAfterWrite) {
            this.cacheRefreshAfterWrite = cacheRefreshAfterWrite;
            return this;
        }

        /**
         * Set cache maximum size.
         * @param cacheMaximumSize Maximum entries to keep in cache
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setCacheMaximumSize(int cacheMaximumSize) {
            this.cacheMaximumSize = cacheMaximumSize;
            return this;
        }

        /**
         * Set retry interval
         * @param milliseconds the retry interval in milliseconds
         * @return {@link ParsecAsyncHttpClient.Builder}
         */
        public Builder setRetryIntervalInMilliSeconds(long milliseconds) {
            this.retryIntervalMillisOpt = Optional.of(milliseconds);
            return this;
        }
    }

    /**
     * simple wrap caffeine CacheStats class, avoid direct expose the interface.
     */
    public static class CacheStats {
        com.github.benmanes.caffeine.cache.stats.CacheStats cacheStats;

        public CacheStats(com.github.benmanes.caffeine.cache.stats.CacheStats cacheStats) {
            this.cacheStats = cacheStats;
        }

        public double hitRate() {
            return this.cacheStats.hitRate();
        }

        public double missRate() {
            return this.cacheStats.missRate();
        }

        public long requestCount() {
            return this.cacheStats.requestCount();
        }

        public long evictionCount() {
            return this.cacheStats.evictionCount();
        }

        public long hitCount() {
            return this.cacheStats.hitCount();
        }

        public long missCount() {
            return this.cacheStats.missCount();
        }

        public double averageLoadPenalty() {
            return this.cacheStats.averageLoadPenalty();
        }

        public double loadCount() {
            return this.cacheStats.loadCount();
        }

        public long loadFailureCount() {
            return this.cacheStats.loadFailureCount();
        }

        public long loadSuccessCount() {
            return this.cacheStats.loadSuccessCount();
        }
    }
}
