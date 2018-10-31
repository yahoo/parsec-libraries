// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.yahoo.parsec.test.WireMockBaseTest;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

/**
 * Created by baiyi on 10/26/2018.
 */
public class ParsecAsyncHttpClientProfileLoggingTest extends WireMockBaseTest {

    private static  ParsecAsyncHttpClient parsecHttpClient =
            new ParsecAsyncHttpClient.Builder()
                    .setAcceptAnyCertificate(true).build();


    Appender mockAppender = mock(Appender.class);
    Map<String, Collection<String>> headers;

    @BeforeTest
    public void setupCommonForAllTests() throws JsonProcessingException {
        headers = new HashMap<>();
        headers.put("header1", Arrays.asList("value1"));
        headers.put("header2", Arrays.asList("value2"));
    }

    @BeforeMethod
    public void setup() {
        Logger logger = (Logger) LoggerFactory.getLogger("parsec.clients.profiling_log");
        mockAppender = mock(Appender.class);
        logger.addAppender(mockAppender);
    }


    @Test
    public void nonCriticalGetShouldBeLogged() throws URISyntaxException, ExecutionException, InterruptedException {

        String url = "/get200Profiling";
        WireMock.stubFor(get(urlEqualTo(url))
                .willReturn(okJson(stubReqBodyJson)));


        Map<String, Collection<String>> headers = new HashMap<>();

        String requestMethod = HttpMethod.GET;

        ParsecAsyncHttpRequest request =
                new ParsecAsyncHttpRequest.Builder()
                        .setUrl(wireMockBaseUrl+url)
                        .setHeaders(headers)
                        .setRequestTimeout(300)
                        .setMethod(requestMethod)
                        .setBody(stubReqBodyJson).setBodyEncoding("UTF-8").build();

        Response response = parsecHttpClient.execute(request).get();

        assertThat(response.getStatus(), equalTo(200));

        then(mockAppender).should().doAppend(argThat(hasToString(containsString(url))));
    }

    @Test
    public void criticalPostRequestShouldBeLogged() throws URISyntaxException, ExecutionException, InterruptedException {

        String url = "/post200Profiling";

        WireMock.stubFor(post(urlEqualTo(url))
                .withRequestBody(equalToJson(stubReqBodyJson))
                .willReturn(okJson(stubRespBodyJson)));

        String requestMethod = HttpMethod.POST;
        Map<String, Collection<String>> headers = new HashMap<>();
        headers.put("key1", Arrays.asList("headerValue"));


        ParsecAsyncHttpRequest request =
                new ParsecAsyncHttpRequest.Builder()
                .setUrl(wireMockBaseUrl+url)
                .setHeaders(headers)
                .setRequestTimeout(300)
                .setMethod(requestMethod)
                .setBody(stubReqBodyJson).setBodyEncoding("UTF-8").build();


        parsecHttpClient.criticalExecute(request).get();

        then(mockAppender).should().doAppend(argThat(hasToString(containsString(url))));

    }


    @Test
    public void postRequestRetriesShouldBeLogged() throws ExecutionException, InterruptedException {


        String url = "/postRequestRetriesProfiling";

        String scenarioName = "postRetries";
        WireMock.stubFor(post(urlEqualTo(url)).inScenario(scenarioName)
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo("requested count 1")
                .withRequestBody(equalToJson(stubReqBodyJson))
                .willReturn(aResponse().withStatus(500)));

        WireMock.stubFor(post(urlEqualTo(url)).inScenario(scenarioName)
                .whenScenarioStateIs("requested count 1")
                .withRequestBody(equalToJson(stubReqBodyJson))
                .willReturn(okJson(stubRespBodyJson)));

        String requestMethod = HttpMethod.POST;
        Map<String, Collection<String>> headers = new HashMap<>();
        headers.put("key1", Arrays.asList("headerValue"));


        ParsecAsyncHttpRequest request =
                new ParsecAsyncHttpRequest.Builder()
                        .setUrl(wireMockBaseUrl+url)
                        .setHeaders(headers)
                        .setRequestTimeout(300)
                        .setMethod(requestMethod)
                        .setMaxRetries(2)
                        .addRetryStatusCode(500)
                        .setBody(stubReqBodyJson).setBodyEncoding("UTF-8").build();


        Response response = parsecHttpClient.criticalExecute(request).get();


        assertThat(response.getStatus(), equalTo(200));
        then(mockAppender).should(times(2)).doAppend(argThat(hasToString(containsString(url))));
    }
}
