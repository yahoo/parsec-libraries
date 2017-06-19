// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * {@link Callable} implementation that handles HTTP request retry based on response status code.
 *
 * @param <T>
 */
class ParsecHttpRequestRetryCallable<T> implements Callable<T> {
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
     * Response list.
     */
    private List<T> responses;

    /**
     * Constructor.
     *
     * @param client client
     * @param request request
     */
    public ParsecHttpRequestRetryCallable(final AsyncHttpClient client, final ParsecAsyncHttpRequest request) {
        this(client, request, null);
    }

    /**
     * Constructor.
     *
     * @param client client
     * @param request request
     * @param asyncHandler async handler
     */
    public ParsecHttpRequestRetryCallable(
            final AsyncHttpClient client, final ParsecAsyncHttpRequest request, final AsyncHandler<T> asyncHandler) {
        this.client = client;
        this.request = request;
        this.asyncHandler = asyncHandler;
        responses = new ArrayList<>();
    }

    /**
     * Check status code and handles retry if T is type {@link Response} or Ning {@link com.ning.http.client.Response}.
     *
     * @return T
     * @throws InterruptedException Interrupted exception
     * @throws ExecutionException Execution exception
     */
    @Override
    public T call() throws InterruptedException, ExecutionException {
        responses.clear();
        final Request ningRequest = request.getNingRequest();
        final List<Integer> retryStatusCodes = request.getRetryStatusCodes();
        final int maxRetries = request.getMaxRetries();

        T response = executeRequest(ningRequest);
        int statusCode = getStatusCode(response);

        if (statusCode == -1) {
            return response;
        }

        int retries = 0;
        responses.add(response);

        while (retryStatusCodes.contains(statusCode)) {
            if (maxRetries > retries) {
                LOGGER.debug("Retry number: " + retries + " (max: " + maxRetries + ")");
                retries++;

                response = executeRequest(ningRequest);
                statusCode = getStatusCode(response);
                responses.add(response);
            } else {
                LOGGER.debug("Max retries reached: " + retries + " (max: " + maxRetries + ")");
                break;
            }
        }

        return response;
    }

    /**
     * Execute Request.
     *
     * @param ningRequest Ning request
     * @return T
     * @throws InterruptedException Interrupted exception
     * @throws ExecutionException Execution exception
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
}
