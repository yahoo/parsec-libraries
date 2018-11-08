// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.filters;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.Request;
import com.ning.http.client.Response;

import java.io.IOException;
import java.util.Map;

/**
 * SEE ALSO com.yahoo.parsec.web.JsonFormatter
 * Created by baiyi on 11/08/2018.
 */
public class NingJsonFormatter implements NingRequestResponseFormatter {
    private static final ObjectMapper _OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Override
    public String format(Request req, Response resp, Map<String, Object> additionalArgs) {
        ObjectNode root = _OBJECT_MAPPER.createObjectNode();
        root.put("time", (System.currentTimeMillis() / 1000L));
        fillNode(req, root.with("request"));
        fillNode(resp, root.with("response"));

        additionalArgs.forEach((key, value) -> root.putPOJO(key, value));

        try {
            return _OBJECT_MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillNode(Request req, ObjectNode node) {

        String url = req.getUrl();
        String queryStr = req.getUri().getQuery();

        node.put("method", req.getMethod());
        node.put("uri", url);
        node.put("query", queryStr == null ? "" : queryStr);

        fillNode(req.getHeaders(), node.with("headers"));

        node.put("payload", req.getStringData());
    }

    private void fillNode(FluentCaseInsensitiveStringsMap headers, ObjectNode node){
        headers.keySet().forEach(
                k -> node.put(k, headers.getJoinedValue(k, ","))
        );
    }


    private void fillNode(Response resp, ObjectNode node) {
        if (resp == null) {
            return;
        }
        node.put("status", resp.getStatusCode());
        fillNode(resp.getHeaders(), node.with("headers"));
        try {
            node.put("payload", resp.getResponseBody("UTF-8"));
        } catch (IOException e) {
            node.put("payload", String.format("unable to retrieve. %s", e.getMessage()));
        }
    }
}
