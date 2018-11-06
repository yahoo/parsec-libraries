/// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.filters;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHandlerExtensions;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.ProgressAsyncHandler;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.yahoo.parsec.clients.ParsecAsyncProgress;
import com.yahoo.parsec.clients.ParsecAsyncProgressTimer;
import com.yahoo.parsec.clients.ParsecClientDefine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;


/**
 * {@link AsyncHandler} wrapper that logs connection related information.
 *
 * @param <T> T
 * @author sho, baiyi
 */
class LoggingAsyncHandlerWrapper<T> implements AsyncHandler<T>, ProgressAsyncHandler<T>, AsyncHandlerExtensions {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAsyncHandlerWrapper.class);


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
    private final Logger traceLogger;
    /**
     * ning http request.
     */
    private Request ningRequest;
    /**
     * parsec async progress do.
     */
    private ParsecAsyncProgress progress;

    /**
     * last resp code.
     */
    private BiPredicate<Request, ResponseOrThrowable> loggingPredicate;
    private NingRequestResponseFormatter formatter;

    /**
     * Constructor.
     *
     * @param asyncHandler asyncHandler
     * @param loggingPredicate
     */
    public LoggingAsyncHandlerWrapper(final AsyncHandler<T> asyncHandler, final Request ningRequest,
                                      BiPredicate<Request, ResponseOrThrowable> loggingPredicate,
                                      NingRequestResponseFormatter formatter,
                                      String loggerName) {

        this.asyncHandler = asyncHandler;
        extensions = (asyncHandler instanceof AsyncHandlerExtensions)
                ? (AsyncHandlerExtensions) asyncHandler : null;
        progressAsyncHandler = (asyncHandler instanceof ProgressAsyncHandler)
                ? (ProgressAsyncHandler<T>) asyncHandler : null;
        this.progress = new ParsecAsyncProgress();
        this.ningRequest = ningRequest;
        this.loggingPredicate = loggingPredicate;
        this.formatter = formatter;
        this.traceLogger = LoggerFactory.getLogger(loggerName);
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
        logResponseOrThrowable(ningRequest, null, t, progress);
        progress.reset();
        ningRequest.getHeaders().replaceWith(ParsecClientDefine.HEADER_PROFILING_LAST_RESPONSE_CODE, String.valueOf(-1));
        asyncHandler.onThrowable(t);
    }



    @Override
    public T onCompleted() throws Exception {
        final Response ningResponse = builder.build();

        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_TOTAL);
        logResponseOrThrowable(ningRequest, ningResponse, null, progress);
        progress.reset();
        int lastRespCode = ningResponse.getStatusCode();
        ningRequest.getHeaders().replaceWith(ParsecClientDefine.HEADER_PROFILING_LAST_RESPONSE_CODE, String.valueOf(lastRespCode));

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


    private void logResponseOrThrowable(Request ningRequest, Response ningResponse, Throwable throwable, ParsecAsyncProgress progress) {

        if (!loggingPredicate.test(ningRequest, new ResponseOrThrowable(ningResponse, throwable))) {
            return;
        }

        Map<String, Object> additionalArgs = createAdditionalArgs(progress,  throwable);
        String messageToLog = formatter.format(ningRequest, ningResponse, additionalArgs);
        if(traceLogger.isTraceEnabled()) {
            traceLogger.trace(messageToLog);
        }
    }

    private Map<String,Object> createAdditionalArgs(ParsecAsyncProgress progress,
                                                    Throwable throwable) {

        Map<String, Object> map = new HashMap();

        map.put(ParsecClientDefine.PROFILING_ASYNC_PROGRESS, progress);
        if (throwable != null) {
            map.put(ParsecClientDefine.RESPONSE_ERROR, throwable);
        }

        return map;
    }


}

