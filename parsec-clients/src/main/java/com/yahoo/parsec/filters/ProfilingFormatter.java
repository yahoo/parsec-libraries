// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.parsec.filters;

import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.yahoo.parsec.clients.ParsecAsyncProgress;
import com.yahoo.parsec.clients.ParsecClientDefine;
import com.yahoo.parsec.clients.ParsecClientProfilingLogUtil;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Map;

/**
 * Created by baiyi on 11/02/2018.
 */
public class ProfilingFormatter implements NingRequestResponseFormatter {
    @Override
    public String format(Request req, Response resp, Map<String, Object> additionalArgs) {
        int requestCount = NumberUtils.toInt(req.getHeaders()
                .getFirstValue(ParsecClientDefine.HEADER_PROFILING_REQUEST_COUNT));

        int lastRespCode = NumberUtils.toInt(req.getHeaders()
                .getFirstValue(ParsecClientDefine.HEADER_PROFILING_LAST_RESPONSE_CODE), 0);

        ParsecAsyncProgress progress = (ParsecAsyncProgress)additionalArgs.get(ParsecClientDefine.PROFILING_ASYNC_PROGRESS);

        String requestStatus = ParsecClientDefine.REQUEST_SINGLE;
        if (requestCount > 1) {
            requestStatus = ParsecClientDefine.REQUEST_SINGLE_RETRY + ":" + lastRespCode;
        }
        return ParsecClientProfilingLogUtil.formatMessage(req, resp, requestStatus, progress, null);
    }
}
