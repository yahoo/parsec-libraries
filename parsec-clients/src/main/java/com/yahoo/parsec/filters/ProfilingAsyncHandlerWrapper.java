package com.yahoo.parsec.filters;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHandlerExtensions;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.ProgressAsyncHandler;
import com.ning.http.client.Request;
import com.yahoo.parsec.clients.ParsecAsyncProgress;
import com.yahoo.parsec.clients.ParsecAsyncProgressTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.function.BiPredicate;

/**
 * Created by baiyi on 11/06/2018.
 */
public class ProfilingAsyncHandlerWrapper<T> extends LoggingAsyncHandlerWrapper<T>
                                             implements ProgressAsyncHandler<T>, AsyncHandlerExtensions {


    private static final Logger LOGGER = LoggerFactory.getLogger(ProfilingAsyncHandlerWrapper.class);



    private final ProgressAsyncHandler<T> progressAsyncHandler;

    private final AsyncHandlerExtensions extensions;

    private final ParsecAsyncProgress progress;

    /**
     * Constructor.
     *
     * @param asyncHandler     asyncHandler
     * @param ningRequest
     * @param loggingPredicate
     * @param formatter
     * @param loggerName
     */
    public ProfilingAsyncHandlerWrapper(AsyncHandler<T> asyncHandler, Request ningRequest,
                                        BiPredicate<Request, ResponseOrThrowable> loggingPredicate,
                                        NingRequestResponseFormatter formatter, String loggerName) {

        super(asyncHandler, ningRequest, loggingPredicate, formatter, loggerName);
        extensions = (asyncHandler instanceof AsyncHandlerExtensions)
                ? (AsyncHandlerExtensions) asyncHandler : null;
        progressAsyncHandler = (asyncHandler instanceof ProgressAsyncHandler)
                ? (ProgressAsyncHandler<T>) asyncHandler : null;
        progress = new ParsecAsyncProgress();
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
     * onSslHandshakeCompleted.
     */
    @Override
    public void onSslHandshakeCompleted() {
        if (extensions != null) {
            extensions.onSslHandshakeCompleted();
        }
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
        ParsecAsyncProgressTimer.progressTime(progress, ParsecAsyncProgressTimer.TimerOpCode.TIMER_STARTTRANSFER);
        return super.onHeadersReceived(headers);
    }
}
