// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by hankting on 9/6/15.
 */
public class ParsecAsyncProgress {
    /**
     * the start single time.
     */
    private long startSingleTime;
    /**
     * the nslookup time.
     */
    private long nsLookupTime;
    /**
     * the connect time.
     */
    private long connectTime;
    /**
     * the pretransfer time.
     */
    private long preTransferTime;
    /**
     * the start transfer time.
     */
    private long startTransferTime;
    /**
     * the total time.
     */
    private long totalTime;

    /**
     * the nslookup_time getter.
     * @return nslookupTime
     */
    @JsonProperty("namelookup_time")
    public long getNsLookupTime() {
        return nsLookupTime;
    }

    /**
     * the nslookup_time setter.
     * @param nsLookupTime nslookup time
     */
    public void setNsLookupTime(long nsLookupTime) {
        this.nsLookupTime = nsLookupTime;
    }

    /**
     * the connect_time getter.
     * @return connectTime
     */
    @JsonProperty("connect_time")
    public long getConnectTime() {
        return connectTime;
    }

    /**
     * the connect_time setter.
     * @param connectTime connect time
     */
    public void setConnectTime(long connectTime) {
        this.connectTime = connectTime;
    }

    /**
     * the pretransfer_time getter.
     * @return pretransferTime
     */
    @JsonProperty("pretransfer_time")
    public long getPreTransferTime() {
        return preTransferTime;
    }

    /**
     * the pretransfer_time setter.
     * @param preTransferTime pretransfer time
     */
    public void setPreTransferTime(long preTransferTime) {
        this.preTransferTime = preTransferTime;
    }

    /**
     * the starttransfer_time getter.
     * @return starttransferTime
     */
    @JsonProperty("starttransfer_time")
    public long getStartTransferTime() {
        return startTransferTime;
    }

    /**
     * the starttransfer_time setter.
     * @param startTransferTime starttransfer tie
     */
    public void setStartTransferTime(long startTransferTime) {
        this.startTransferTime = startTransferTime;
    }

    /**
     * the startsingle_time getter.
     * @return startsingleTime
     */
    @JsonIgnore
    public long getStartSingleTime() {
        return startSingleTime;
    }

    /**
     * the startsingle_time setter.
     * @param startSingleTime start single time
     */
    public void setStartSingleTime(long startSingleTime) {
        this.startSingleTime = startSingleTime;
    }

    /**
     * the total_time getter.
     * @return totalTime
     */
    @JsonProperty("total_time")
    public long getTotalTime() {
        return this.totalTime;
    }

    /**
     * the total_time setter.
     * @param totalTime total time
     */
    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    /**
     * reset.
     */
    public void reset() {
        this.startSingleTime = 0;
        this.nsLookupTime = 0;
        this.connectTime = 0;
        this.preTransferTime = 0;
        this.startTransferTime = 0;
        this.totalTime = 0;
    }
}
