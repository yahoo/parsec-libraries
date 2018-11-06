// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.web

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.Context
import ch.qos.logback.core.LogbackException
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.core.status.Status

class MockAppender implements Appender<ILoggingEvent> {

    String cachedMsg;

    @Override
    String getName() {
        return null
    }

    @Override
    void doAppend(ILoggingEvent event) throws LogbackException {
        cachedMsg = event.getMessage();
    }

    public void setCacheMsg(String msg) {
        cachedMsg = msg;
    }

    public String getCacheMsg() {
        return cachedMsg;
    }

    @Override
    void setName(String name) {

    }

    @Override
    void setContext(Context context) {

    }

    @Override
    Context getContext() {
        return null
    }

    @Override
    void addStatus(Status status) {

    }

    @Override
    void addInfo(String msg) {

    }

    @Override
    void addInfo(String msg, Throwable ex) {

    }

    @Override
    void addWarn(String msg) {

    }

    @Override
    void addWarn(String msg, Throwable ex) {

    }

    @Override
    void addError(String msg) {

    }

    @Override
    void addError(String msg, Throwable ex) {

    }

    @Override
    void addFilter(Filter<ILoggingEvent> newFilter) {

    }

    @Override
    void clearAllFilters() {

    }

    @Override
    List<Filter<ILoggingEvent>> getCopyOfAttachedFiltersList() {
        return null
    }

    @Override
    FilterReply getFilterChainDecision(ILoggingEvent event) {
        return null
    }

    @Override
    void start() {

    }

    @Override
    void stop() {

    }

    @Override
    boolean isStarted() {
        return false
    }
}
