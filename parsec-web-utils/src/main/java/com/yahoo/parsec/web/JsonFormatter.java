// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

public class JsonFormatter implements ServletRequestResponseFomatter {

    private static final ObjectMapper _OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Override
    public String format(ParsecServletRequestWrapper req, ParsecServletResponseWrapper resp,
                         Map<String, Object> additionalArgs) {
        try {
            ObjectNode root = _OBJECT_MAPPER.createObjectNode();
            ObjectNode reqNode = root.with("request");
            fillRequestNode(req, reqNode);
            ObjectNode respNode = root.with("response");
            fillResponseNode(resp, respNode);
            return _OBJECT_MAPPER.writeValueAsString(root);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillRequestNode(ParsecServletRequestWrapper req, ObjectNode reqNode) throws IOException {
        reqNode.put("method", req.getMethod());
        reqNode.put("uri", req.getRequestURI());
        reqNode.put("query", req.getQueryString());
        reqNode.set("headers", getReqHeadersNode(req));
        reqNode.put("payload", req.getContent());
    }

    private void fillResponseNode(ParsecServletResponseWrapper resp, ObjectNode respNode) throws IOException {
        respNode.put("status", resp.getStatus());
        respNode.set("headers", getRespHeadersNode(resp));
        respNode.put("payload", resp.getContent());
    }

    private ObjectNode getReqHeadersNode(ParsecServletRequestWrapper request) {
        ObjectNode headersNode = _OBJECT_MAPPER.createObjectNode();
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String val = request.getHeader(key);
            headersNode.put(key, val);
        }
        return headersNode;
    }

    private ObjectNode getRespHeadersNode(ParsecServletResponseWrapper response) {
        ObjectNode headersNode = _OBJECT_MAPPER.createObjectNode();
        Collection<String> headerNames = response.getHeaderNames();
        for (String header: headerNames) {
            headersNode.put(header, response.getHeader(header));
        }
        return headersNode;
    }
}