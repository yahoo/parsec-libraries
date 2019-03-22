// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.http.client.extra.ListenableFutureAdapter;

import java.util.concurrent.*;

/**
 * Simple {@link CompletableFuture} implementation that wraps {@link Future}.
 *
 * @param <T> Response type
 */
public class ParsecCompletableFuture<T> extends CompletableFuture<T> {
    /**
     * Future.
     */
    private Future<T> future;

    /**
     * Constructor.
     *
     * @param future future
     */
    public ParsecCompletableFuture(final Future<T> future) {
        if (future instanceof com.ning.http.client.ListenableFuture) {
            this.future = ListenableFutureAdapter.asGuavaFuture((com.ning.http.client.ListenableFuture<T>) future);
        } else {
            this.future = future;
        }

        if (this.future instanceof ListenableFuture) {
            Futures.addCallback((ListenableFuture<T>) this.future, new FutureCallback<T>() {
                @Override
                public void onSuccess(T result) {
                    complete(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    completeExceptionally(t);
                }
            },
            MoreExecutors.directExecutor());
        }
    }

    /**
     * Cancel.
     *
     * @param mayInterruptIfRunning mayInterruptIfRunning
     * @return cancel was success or not
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    /**
     * Get.
     *
     * @return T
     * @throws InterruptedException Interrupted exception
     * @throws ExecutionException Execution exception
     */
    @Override
    public T get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    /**
     * Get.
     *
     * @param timeout timeout
     * @param unit unit
     * @return T
     * @throws InterruptedException Interrupted exception
     * @throws TimeoutException Timeout exception
     * @throws ExecutionException Execution exception
     */
    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
        return future.get(timeout, unit);
    }

    /**
     * Is cancelled.
     *
     * @return boolean
     */
    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    /**
     * Is done.
     *
     * @return boolean
     */
    @Override
    public boolean isDone() {
        return future.isDone();
    }

}
