// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.filters;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.yahoo.parsec.clients.ParsecAsyncHttpClient;
import com.yahoo.parsec.clients.ParsecAsyncHttpRequest;
import com.yahoo.parsec.test.WireMockBaseTest;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;

/**
 * Created by baiyi on 10/30/2018.
 */
public class RequestResponeLoggingFilterTest extends WireMockBaseTest {

    private static ParsecAsyncHttpClient parsecHttpClient =
            new ParsecAsyncHttpClient.Builder()
                    .setAcceptAnyCertificate(true)
                    .addRequestFilter(new RequestResponeLoggingFilter())
                    .build();



    Appender mockAppender = mock(Appender.class);
    Map<String, Collection<String>> headers;

    @BeforeTest
    public void setupCommon() throws JsonProcessingException {

        headers = new HashMap<>();
        headers.put("header1", Arrays.asList("value1"));
        headers.put("header2", Arrays.asList("value2"));
    }


    @BeforeMethod
    public void setup() {
        //todo: modify the logger name later.
        Logger logger = (Logger) LoggerFactory.getLogger("parsec.clients.reqresp_log");

        mockAppender = mock(Appender.class);
        logger.addAppender(mockAppender);
    }

    @Test
    public void successPostRequestsShouldBeLogged() throws URISyntaxException, ExecutionException, InterruptedException {

        String url = "/postWithFilter200";
        WireMock.stubFor(post(urlEqualTo(url))
                .withRequestBody(equalToJson(stubReqBodyJson))
                .willReturn(okJson(stubRespBodyJson)));

        String requestMethod = HttpMethod.POST;

        Map<String, Collection<String>> headers = new HashMap<>();

        ParsecAsyncHttpRequest request =
                new ParsecAsyncHttpRequest.Builder()
                        .setUrl(wireMockBaseUrl+url)
                        .setHeaders(headers)
                        .setRequestTimeout(300)
                        .setMethod(requestMethod)
                        .setBody(stubReqBodyJson).setBodyEncoding("UTF-8").build();

        Response response = parsecHttpClient.criticalExecute(request).get();

        then(mockAppender).should().doAppend(argThat(hasToString(containsString(url))));

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getEntity(), is(notNullValue()));
    }

    @Test
    public void faultPostRequestShouldBeLogged() throws URISyntaxException {

        String url = "/postWithFilterAtFault";
        WireMock.stubFor(post(urlEqualTo(url))
                .withRequestBody(equalToJson(stubReqBodyJson))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        String requestMethod = HttpMethod.POST;



        ParsecAsyncHttpRequest request =
                new ParsecAsyncHttpRequest.Builder()
                        .setUrl(wireMockBaseUrl+url)
                        .setHeaders(headers)
                        .setRequestTimeout(300)
                        .setMethod(requestMethod)
                        .setBody(stubReqBodyJson).setBodyEncoding("UTF-8").build();

        Throwable exception = null;
        try {
            parsecHttpClient.criticalExecute(request).get();
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));
        then(mockAppender).should().doAppend(argThat(hasToString(containsString(url))));
    }


    //todo: later
    //@Test
    public void getRequestShouldNotBeLogged() {

    }

}
