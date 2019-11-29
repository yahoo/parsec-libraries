// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * {@link Callable} implementation that handles HTTP request retry based on response status code.
 */
class ParsecHttpRequestRetryCallable<T> implements Callable<T> {

    /**
     * By default, do not delay the retries.
     */
    private static final long DEFAULT_RETRY_INTERVAL = 0;

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ParsecHttpRequestRetryCallable.class);

    /**
     * Async handler.
     */
    private final AsyncHandler<T> asyncHandler;

    /**
     * Ning client.
     */
    private final AsyncHttpClient client;

    /**
     * Request.
     */
    private final ParsecAsyncHttpRequest request;

    /**
     * For delayed retries
     */
    private RetryDelayer retryDelayer;

    /**
     * Response list.
     */
    private List<T> responses;

    /**
     * Constructor.
     *
     * @param client  client
     * @param request request
     */
    public ParsecHttpRequestRetryCallable(final AsyncHttpClient client, final ParsecAsyncHttpRequest request) {
        this(client, request, null);
    }

    /**
     * Constructor.
     *
     * @param client       client
     * @param request      request
     * @param asyncHandler async handler
     */
    public ParsecHttpRequestRetryCallable(
        final AsyncHttpClient client, final ParsecAsyncHttpRequest request, final AsyncHandler<T> asyncHandler) {
        this(client, request, asyncHandler, DEFAULT_RETRY_INTERVAL);
    }

    /**
     * Constructor.
     *
     * @param client              client
     * @param request             request
     * @param asyncHandler        async handler
     * @param retryIntervalMillis retry interval in milliseconds
     */
    public ParsecHttpRequestRetryCallable(
        final AsyncHttpClient client,
        final ParsecAsyncHttpRequest request,
        final AsyncHandler<T> asyncHandler,
        long retryIntervalMillis
    ) {
        this.client = client;
        this.request = request;
        this.asyncHandler = asyncHandler;
        this.retryDelayer = new RetryDelayer(retryIntervalMillis);
        responses = new ArrayList<>();
    }

    /**
     * Package-private Constructor. Only for unit-test usage.
     *
     * @param client              client
     * @param request             request
     * @param asyncHandler        async handler
     * @param retryDelayer        custom retry Delayer
     */
    ParsecHttpRequestRetryCallable(
        final AsyncHttpClient client,
        final ParsecAsyncHttpRequest request,
        final AsyncHandler<T> asyncHandler,
        RetryDelayer retryDelayer
    ) {
        this.client = client;
        this.request = request;
        this.asyncHandler = asyncHandler;
        this.retryDelayer = retryDelayer;
        responses = new ArrayList<>();
    }

    /**
     * Check status code and handles retry if T is type {@link Response} or Ning {@link com.ning.http.client.Response}.
     *
     * @return T
     * @throws Exception exception
     */
    @Override
    public T call() throws Exception {
        responses.clear();
        final Request ningRequest = request.getNingRequest();
        final List<Integer> retryStatusCodes = request.getRetryStatusCodes();
        final List<Class<? extends Throwable>> retryExceptions = request.getRetryExceptions();
        final int maxRetries = request.getMaxRetries();

        T response;
        Exception exception;
        int retries = 0;
        for (; ; ) {
            response = null;
            exception = null;
            try {
                // when this is not the first try (i.e. is re-trying), delay an interval
                if (retries > 0) {
                    retryDelayer.delay();
                }

                response = executeRequest(ningRequest);
                responses.add(response);
                int statusCode = getStatusCode(response);
                if (statusCode == -1 || !retryStatusCodes.contains(statusCode)) {
                    break;
                }
            } catch (Exception e) {
                Throwable root = ExceptionUtils.getRootCause(e);
                if (!retryExceptions.contains(root.getClass())) {
                    throw e;
                }
                exception = e;
            }

            if (retries == maxRetries) {
                LOGGER.debug("Max retries reached: " + retries + " (max: " + maxRetries + ")");
                break;
            }
            retries++;
            LOGGER.debug("Retry number: " + retries + " (max: " + maxRetries + ")");
        }
        if (exception != null) {
            throw exception;
        }
        return response;
    }

    /**
     * Execute Request.
     *
     * @param ningRequest Ning request
     * @return T
     * @throws InterruptedException Interrupted exception
     * @throws ExecutionException   Execution exception
     */
    @SuppressWarnings("unchecked")
    private T executeRequest(Request ningRequest) throws InterruptedException, ExecutionException {
        if (asyncHandler != null) {
            return client.executeRequest(ningRequest, asyncHandler).get();
        } else {
            return (T) client.executeRequest(ningRequest).get();
        }
    }

    /**
     * Get Responses.
     *
     * @return {@literal List<T>}
     */
    public List<T> getResponses() {
        return responses;
    }

    /**
     * Gets Status Code from {@link Response} or Ning {@link com.ning.http.client.Response}.
     *
     * @param response T
     * @return status code or -1 if T is not type of {@link Response} or Ning {@link com.ning.http.client.Response}
     */
    private int getStatusCode(T response) {
        if (response instanceof Response) {
            return ((Response) response).getStatus();
        } else if (response instanceof com.ning.http.client.Response) {
            return ((com.ning.http.client.Response) response).getStatusCode();
        } else {
            return -1;
        }
    }

    /**
     * Package-private, this is only for unit-testing.
     * @return retryDelayer
     */
    RetryDelayer getRetryDelayer() {
        return retryDelayer;
    }

    /**
     * Extract delay mechanism as a class to let it be testable
     */
    public class RetryDelayer {
        private final long retryIntervalMillis;

        RetryDelayer(long retryIntervalMillis) {
            this.retryIntervalMillis = retryIntervalMillis;
        }

        public void delay() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(retryIntervalMillis);
        }

        /**
         * Package-private, this is only for unit-testing.
         * @return retryIntervalMillis
         */
        long getRetryIntervalMillis() {
            return retryIntervalMillis;
        }
    }
}
