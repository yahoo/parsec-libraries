package com.yahoo.parsec.filters;

import com.ning.http.client.Request;
import com.ning.http.client.Response;

import java.util.Map;

/**
 * Created by baiyi on 11/02/2018.
 */
public class ProfilingFormatter implements NingRequestResponseFormatter {
    @Override
    public String format(Request req, Response resp, Map<String, Object> additionalArgs) {
        return null;
    }
}
