// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.concurrent.*;


/**
 * An asynchronous loading cache.
 * key: {@link ParsecAsyncHttpRequest}
 * value: CompletableFuture&lt;{@link Response}&gt;.
 *
 * @author sho
 */
@SuppressWarnings("unused")
final class ParsecAsyncHttpResponseLoadingCache {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ParsecAsyncHttpResponseLoadingCache.class);

    /**
     * Base async loading cache.
     */
    private AsyncLoadingCache<ParsecAsyncHttpRequest, Response> asyncLoadingCache;

    /**
     * Response cache loader.
     */
    private ResponseCacheLoader responseCacheLoader;

    /**
     * Executor for cleaning up cache periodically.
     */
    private ScheduledExecutorService cleanUpExecutorService;

    /**
     * Unused private constructor.
     */
    private ParsecAsyncHttpResponseLoadingCache() {

    }

    /**
     * Private constructor.
     *
     * @param builder builder
     */
    @SuppressWarnings("unchecked")
    private ParsecAsyncHttpResponseLoadingCache(final Builder builder) {
        responseCacheLoader = new ResponseCacheLoader(builder.client);
        asyncLoadingCache = builder.caffeine.buildAsync(responseCacheLoader);

        cleanUpExecutorService = Executors.newSingleThreadScheduledExecutor();
        cleanUpExecutorService.scheduleWithFixedDelay(() -> {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Performing cache cleanup");
                }
                asyncLoadingCache.synchronous().cleanUp();
            },
            builder.cleanUpInterval, builder.cleanUpInterval, builder.cleanUpTimeUnit
        );
    }

    /**
     * Get from cache.
     *
     * @param request Request to lookup in cache
     * @return CompletableFuture&lt;{@link Response}&gt;
     */
    public CompletableFuture<Response> get(ParsecAsyncHttpRequest request) {
        return asyncLoadingCache.get(request);
    }

    /**
     * Get from cache.
     *
     * @param request Request to lookup in cache
     * @param executorService Executor service to use for asynchronous load
     * @return CompletableFuture&lt;{@link Response}&gt;
     */
    public CompletableFuture<Response> get(ParsecAsyncHttpRequest request, ExecutorService executorService) {
        return asyncLoadingCache.get(request, (k, e) -> responseCacheLoader.asyncLoad(request, executorService));
    }

    /**
     * Get from cache or null if not in cache.
     *
     * @param request Request to lookup in cache
     * @return CompletableFuture&lt;{@link Response}&gt; or null if not in cache
     */
    public CompletableFuture<Response> getIfPresent(ParsecAsyncHttpRequest request) {
        return asyncLoadingCache.getIfPresent(request);
    }

    /**
     * Put CompletableFuture&lt;{@link Response}&gt; into cache.
     *
     * @param request Request to use as cache key
     * @param completableFuture CompletableFuture&lt;{@link Response}&gt; to store
     */
    public void put(ParsecAsyncHttpRequest request, CompletableFuture<Response> completableFuture) {
        asyncLoadingCache.put(request, completableFuture);
    }

    /**
     * Returns a view of the entries stored in this cache as a synchronous {@link LoadingCache}.
     * A mapping is not present if the value is currently being loaded. Modifications made to the
     * synchronous cache directly affect the asynchronous cache. If a modification is made to a
     * mapping that is currently loading, the operation blocks until the computation completes.
     *
     * @return a thread-safe synchronous view of this cache
     */
    public LoadingCache<ParsecAsyncHttpRequest, Response> synchronous() {
        return asyncLoadingCache.synchronous();
    }

    /**
     * Shutdown.
     */
    void shutdownCleanUpExecutorService() {
        if (!cleanUpExecutorService.isShutdown()) {
            cleanUpExecutorService.shutdown();
        }
    }

    /**
     * Static Builder class for {@link ParsecAsyncHttpResponseLoadingCache}.
     *
     * @author sho
     */
    public static class Builder {
        /**
         * Default clean up interval.
         */
        private static final long DEFAULT_CLEANUP_INTERVAL = 60;

        /**
         * Caffeine.
         */
        private Caffeine caffeine;

        /**
         * Client.
         */
        private ParsecAsyncHttpClient client;

        /**
         * Clean up interval.
         */
        private long cleanUpInterval;

        /**
         * Clean up time unit.
         */
        private TimeUnit cleanUpTimeUnit;

        /**
         * Constructor.
         * @param client client
         */
        public Builder(final ParsecAsyncHttpClient client) {
            caffeine = Caffeine.newBuilder();
            this.client = client;
            cleanUpInterval = DEFAULT_CLEANUP_INTERVAL;
            cleanUpTimeUnit = TimeUnit.SECONDS;
        }

        /**
         * Build new {@link ParsecAsyncHttpResponseLoadingCache} instance.
         *
         * @return new {@link ParsecAsyncHttpResponseLoadingCache} instance
         */
        public ParsecAsyncHttpResponseLoadingCache build() {
            return new ParsecAsyncHttpResponseLoadingCache(this);
        }

        /**
         * Set executor {@link Executor}.
         *
         * @param executor Executor for asynchronous cache loading
         * @return {@link ParsecAsyncHttpResponseLoadingCache.Builder}
         */
        public Builder executor(final Executor executor) {
            caffeine.executor(executor);
            return this;
        }

        /**
         * Set cache refresh after write duration.
         * It will make a key become eligible for refresh, but actually be initiated when the entry is queried.
         *
         * @param duration Refresh duration
         * @param unit Refresh time unit
         * @return {@link ParsecAsyncHttpResponseLoadingCache.Builder}
         */
        public Builder refreshAfterWrite(final long duration, final TimeUnit unit) {
            caffeine.refreshAfterWrite(duration, unit);
            return this;
        }

        /**
         * Set cache expire after write duration.
         *
         * @param duration Expire duration
         * @param unit Expire time unit
         * @return {@link ParsecAsyncHttpResponseLoadingCache.Builder}
         */
        public Builder expireAfterWrite(final long duration, final TimeUnit unit) {
            caffeine.expireAfterWrite(duration, unit);
            return this;
        }

        /**
         * Set cache maximum size.
         *
         * @param size Cache maximum size
         * @return {@link ParsecAsyncHttpResponseLoadingCache.Builder}
         */
        public Builder maximumSize(final long size) {
            caffeine.maximumSize(size);
            return this;
        }

        public Builder recordStats() {
            caffeine.recordStats();
            return this;
        }

        /**
         * Set cache clean up interval.
         *
         * @param interval interval
         * @param unit time unit
         * @return {@link ParsecAsyncHttpResponseLoadingCache.Builder}
         */
        public Builder cleanUpInterval(final long interval, final TimeUnit unit) {
            cleanUpInterval = interval;
            cleanUpTimeUnit = unit;
            return this;
        }
    }

    /**
     * {@link CacheLoader} implementation that loads {@link Response} into {@link ParsecAsyncHttpResponseLoadingCache}.
     */
    private final class ResponseCacheLoader implements CacheLoader<ParsecAsyncHttpRequest, Response> {
        /**
         * Client.
         */
        private ParsecAsyncHttpClient client;

        /**
         * Private constructor.
         *
         * @param client client
         */
        private ResponseCacheLoader(final ParsecAsyncHttpClient client) {
            this.client = client;
        }

        /**
         * Load {@link Response} into {@link ParsecAsyncHttpResponseLoadingCache}.
         *
         * @param request {@link ParsecAsyncHttpRequest} as {@link ParsecAsyncHttpResponseLoadingCache} key
         * @return {@link Response}
         * @throws RuntimeException Run time exception
         */
        public Response load(final ParsecAsyncHttpRequest request) throws RuntimeException {
            try {
                return client.criticalExecute(request).get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
