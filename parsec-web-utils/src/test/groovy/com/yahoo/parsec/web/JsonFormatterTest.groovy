// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.web

import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JsonFormatterTest extends Specification {
    def "call GetReqHeadersNode() with ParsecServletRequestWrapper should return json node as expected"() {
        given:
        def mockReq = Mock(HttpServletRequest)
        mockReq.getHeaderNames() >> Collections.enumeration(Arrays.asList(header))
        mockReq.getHeaders(header) >> Collections.enumeration(headerValues)
        def reqWrapper = new ParsecServletRequestWrapper(mockReq)

        when:
        def json = new JsonFormatter().getReqHeadersNode(reqWrapper)

        then:
        json.toString() == expectedResult

        where:
        header   | headerValues                      | expectedResult
        "header" | ["headerValue1", "headerValue2"]  | '{"header":"headerValue1,headerValue2"}'
        "key"    | ["value"]                         | '{"key":"value"}'
    }

    def "call GetRespHeadersNode() with HttpServletResponse should return json node as expected"() {
        given:
        def mockResp = Mock(HttpServletResponse)
        mockResp.getHeaderNames() >> headers
        mockResp.getHeaders(_) >> headerValues
        def respWrapper = new ParsecServletResponseWrapper(mockResp)

        when:
        def json = new JsonFormatter().getRespHeadersNode(respWrapper)

        then:
        json.toString() == expectedResult

        where:
        headers | headerValues | expectedResult
        ["header"] | ["headerValue1", "headerValue2"] | '{"header":"headerValue1,headerValue2"}'
        ["key1","key2"] | ["val"] | '{"key1":"val","key2":"val"}'
    }
}
