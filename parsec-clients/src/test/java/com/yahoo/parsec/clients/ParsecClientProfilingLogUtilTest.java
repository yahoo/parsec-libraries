// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Appender;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.client.uri.Uri;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by hankting on 9/11/15.
 */
public class ParsecClientProfilingLogUtilTest {
    static {
        System.setProperty("logback.configurationFile", "src/test/resources/logback-enableProfiling.xml");
    }

    Request req ;
    Response resp;
    String reqStatus;
    ParsecAsyncProgress progress;
    Appender mockAppender = mock(Appender.class);

    @BeforeTest
    public void setupCommon(){
        req = mock(Request.class);
        when(req.getUri()).thenReturn(Uri.create("http://test.yahoo.com"));
        when(req.getMethod()).thenReturn("POST");
        when(req.getHeaders()).thenReturn(new FluentCaseInsensitiveStringsMap().add(ParsecClientDefine.HEADER_HOST,
                Arrays.asList("dummyhost")));

        resp = mock(Response.class);
        when(resp.getHeader(anyString())).thenReturn("mock resp header");
        when(resp.getHeader("content-length")).thenReturn("100");
        when(resp.getStatusCode()).thenReturn(200);



        reqStatus = "test_status";
        progress = new ParsecAsyncProgress();

    }

    @BeforeMethod
    public void setup(){
        Logger logger = (Logger) LoggerFactory.getLogger("parsec.clients.profiling_log");
        mockAppender = mock(Appender.class);
        logger.addAppender(mockAppender);
    }
    

    @Test
    public void testLogRemoteRequestAndResponse() {
        ParsecClientProfilingLogUtil.logRemoteRequest(req, resp, reqStatus, progress);
        String expectedSubString = "req_url=http://test.yahoo.com, req_host_header=dummyhost, req_method=POST, exec_info={\"namelookup_time\":0,\"connect_time\":0,\"pretransfer_time\":0,\"starttransfer_time\":0,\"total_time\":0}, resp_code=200, src_url=, req_status=test_status, content_length=100, origin=,";


        then(mockAppender).should().doAppend(argThat(hasToString(containsString(expectedSubString))));
    }


    @Test
    public void testLogRemoteRequestOnly() {

        String expectedSubString = "req_url=http://test.yahoo.com, req_host_header=dummyhost, req_method=POST, exec_info={\"namelookup_time\":0,\"connect_time\":0,\"pretransfer_time\":0,\"starttransfer_time\":0,\"total_time\":0}, resp_code=-1, src_url=, req_status=test_status, content_length=, origin=,";
        ParsecClientProfilingLogUtil.logRemoteRequest(req, null, reqStatus, progress);
        then(mockAppender).should().doAppend(argThat(hasToString(containsString(expectedSubString))));
    }
}
