// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.ning.http.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * {@link AsyncHandler} wrapper that logs connection related information.
 * @deprecated  Look into com.yahoo.parsec.filters.LoggingAsyncHandlerWrapper instead
 *
 * @param <T> T
 * @author sho
 */
class ParsecAsyncHandlerWrapper<T> implements AsyncHandler<T>, ProgressAsyncHandler<T>, AsyncHandlerExtensions {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ParsecAsyncHandlerWrapper.class);

    /**
     * response builder.
     */
    private final Response.ResponseBuilder builder = new Response.ResponseBuilder();

    /**
     * asyncHandler.
     */
    private final AsyncHandler<T> asyncHandler;

    /**
     * asyncHandler.
     */
    private final ProgressAsyncHandler<T> progressAsyncHandler;

    /**
     * extensions.
     */
    private final AsyncHandlerExtensions extensions;

    /**
     * ning http request.
     */
    private Request ningRequest;

    /**
     * parsec async progress do.
     */
    private ParsecAsyncProgress progress;
    /**
     * retry count.
     */
    private int requestCount;

    /**
     * last resp code.
     */
    private int lastRespCode;

    /**
     * Constructor.
     *
     * @param asyncHandler asyncHandler
     */
    public ParsecAsyncHandlerWrapper(final AsyncHandler<T> asyncHandler, final Request ningRequest) {
        this.asyncHandler = asyncHandler;
        extensions = (asyncHandler instanceof AsyncHandlerExtensions)
            ? (AsyncHandlerExtensions) asyncHandler : null;
        progressAsyncHandler = (asyncHandler instanceof ProgressAsyncHandler)
            ? (ProgressAsyncHandler<T>) asyncHandler : null;
        this.progress = new ParsecAsyncProgress();
        this.ningRequest = ningRequest;
        this.requestCount = 0;
    }

    /**
     * onBodyPartReceived.
     *
     * @param bodyPart body part
     * @return STATE
     * @throws Exception exception
     */
    @Override
    public STATE onBodyPartReceived(final HttpResponseBodyPart bodyPart) throws Exception {
        builder.accumulate(bodyPart);
        return asyncHandler.onBodyPartReceived(bodyPart);
    }

    /**
     * onConnectionOpen.
     */
    @Override
    public void onConnectionOpen() {
        LOGGER.debug("onConnectionOpen: " + System.currentTimeMillis());
        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_CONNECT);
        if (extensions != null) {
            extensions.onConnectionOpen();
        }
    }

    /**
     * onConnectionPooled.
     */
    @Override
    public void onConnectionPooled() {
        LOGGER.debug("onConnectionPooled: " + System.currentTimeMillis());
        if (extensions != null) {
            extensions.onConnectionPooled();
        }
    }

    /**
     * onDnsResolved.
     *
     * @param inetAddress address
     */
    @Override
    public void onDnsResolved(final InetAddress inetAddress) {
        LOGGER.debug("onDnsResolved: " + System.currentTimeMillis());
        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_NAMELOOKUP);
        if (extensions != null) {
            extensions.onDnsResolved(inetAddress);
        }
    }

    /**
     * onHeadersReceived.
     * @param headers headers
     * @return STATE
     * @throws Exception exception
     */
    @Override
    public STATE onHeadersReceived(final HttpResponseHeaders headers) throws Exception {
        builder.accumulate(headers);
        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_STARTTRANSFER);
        return asyncHandler.onHeadersReceived(headers);
    }

    /**
     * onOpenConnection.
     */
    public void onOpenConnection() {
        LOGGER.debug("onOpenConnection: " + System.currentTimeMillis());
        if (extensions != null) {
            extensions.onOpenConnection();
        }
    }

    /**
     * onPoolConnection.
     */
    public void onPoolConnection() {
        LOGGER.debug("onPoolConnection: " + System.currentTimeMillis());
        requestCount++;
        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_STARTSINGLE);
        if (extensions != null) {
            extensions.onPoolConnection();
        }
    }

    /**
     * onRetry.
     */
    public void onRetry() {
        if (extensions != null) {
            extensions.onRetry();
        }
    }

    /**
     * onSendRequest.
     * @param request request
     */
    public void onSendRequest(final Object request) {
        LOGGER.debug("onSendRequest: " + System.currentTimeMillis());
        if (extensions != null) {
            extensions.onSendRequest(request);
        }
    }

    /**
     * onStatusReceived.
     *
     * @param responseStatus response status
     * @return STATE
     * @throws Exception exception
     */
    public STATE onStatusReceived(final HttpResponseStatus responseStatus) throws Exception {
        builder.reset();
        builder.accumulate(responseStatus);
        return asyncHandler.onStatusReceived(responseStatus);
    }

    /**
     * onHeaderWriteCompleted.
     * @return STATE
     */
    @Override
    public STATE onHeaderWriteCompleted() {
        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_PRETRANSFER);
        if (progressAsyncHandler != null) {
            return progressAsyncHandler.onHeaderWriteCompleted();
        }
        return null;
    }

    /**
     * onContentWriteCompleted.
     * @return STATE
     */
    @Override
    public STATE onContentWriteCompleted() {
        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_PRETRANSFER);
        if (progressAsyncHandler != null) {
            return progressAsyncHandler.onContentWriteCompleted();
        }
        return null;
    }

    /**
     * onContentWriteProgress.
     * @param amount amount
     * @param current current
     * @param total total
     * @return STATE
     */
    public STATE onContentWriteProgress(long amount, long current, long total) {
        if (progressAsyncHandler != null) {
            return progressAsyncHandler.onContentWriteProgress(amount, current, total);
        }
        return null;
    }

    /**
     * onThrowable.
     * @param t throwable
     */
    @Override
    public void onThrowable(Throwable t) {
        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_TOTAL);
        writeProfilingLog(null);
        progress.reset();
        lastRespCode = -1;
        asyncHandler.onThrowable(t);
    }

    @Override
    public T onCompleted() throws Exception {
        final Response ningResponse = builder.build();

        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_TOTAL);
        writeProfilingLog(ningResponse);
        progress.reset();
        lastRespCode = ningResponse.getStatusCode();

        return asyncHandler.onCompleted();
    }

    /**
     * onSslHandshakeCompleted.
     */
    @Override
    public void onSslHandshakeCompleted() {
        if (extensions != null) {
            extensions.onSslHandshakeCompleted();
        }
    }

    /**
     * the progress getter.
     *
     * @return progress
     */
    public ParsecAsyncProgress getProgress() {
        return this.progress;
    }

    /**
     * write log profiling.
     *
     * @param ningResponse ning response
     */
    private void writeProfilingLog(
            final Response ningResponse) {
        String requestStatus = ParsecClientDefine.REQUEST_SINGLE;
        if (requestCount > 1) {
            requestStatus = ParsecClientDefine.REQUEST_SINGLE_RETRY + ":" + lastRespCode;
        }
        ParsecClientProfilingLogUtil.logRemoteRequest(
                ningRequest,
                ningResponse,
                requestStatus,
                progress
        );
    }
}
