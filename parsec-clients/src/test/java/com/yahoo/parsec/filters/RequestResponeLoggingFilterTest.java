// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.filters;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.yahoo.parsec.clients.ParsecAsyncHttpClient;
import com.yahoo.parsec.clients.ParsecAsyncHttpRequest;
import com.yahoo.parsec.test.WireMockBaseTest;
import net.javacrumbs.jsonunit.JsonMatchers;
import org.apache.commons.lang3.StringEscapeUtils;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

/**
 * Test along with the default predicate and jsonformatter
 *
 * Created by baiyi on 10/30/2018.
 */
public class RequestResponeLoggingFilterTest extends WireMockBaseTest {


    static ObjectMapper _OBJECT_MAPPER = new ObjectMapper();
    private ParsecAsyncHttpClient parsecHttpClient;
    NingRequestResponseFormatter jsonFormatter = new NingJsonFormatter();
    Appender mockAppender = mock(Appender.class);

    Map<String, Collection<String>> stubHeaders;

    @BeforeTest
    public void setupCommon() throws JsonProcessingException {
        stubHeaders = new HashMap<>();
        stubHeaders.put("header2", Arrays.asList("value2-1", "value2-2"));
        stubHeaders.put("header1", Arrays.asList("value1"));
    }

    @BeforeTest
    public void setupOnce(){
        parsecHttpClient = new ParsecAsyncHttpClient.Builder()
                .setAcceptAnyCertificate(true)
                .addRequestFilter(new RequestResponeLoggingFilter(jsonFormatter))
                .build();
    }

    @BeforeMethod
    public void setup() {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestResponeLoggingFilter.DEFAULT_TRACE_LOGGER_NAME);
        mockAppender = mock(Appender.class);
        logger.addAppender(mockAppender);
    }

    @Test
    public void getRequestShouldNotBeLogged() throws ExecutionException, InterruptedException {

        String url = "/getWithFilter200?param1=value1";
        WireMock.stubFor(get(urlEqualTo(url))
                .willReturn(okJson(stubRespBodyJson)));

        String requestMethod = HttpMethod.GET;

        Map<String, Collection<String>> headers = stubHeaders;

        ParsecAsyncHttpRequest request =
                new ParsecAsyncHttpRequest.Builder()
                        .setUrl(wireMockBaseUrl+url)
                        .setHeaders(headers)
                        .setRequestTimeout(30)
                        .setMethod(requestMethod)
                        .setBody("").setBodyEncoding("UTF-8").build();

        Response response = parsecHttpClient.criticalExecute(request).get();
        assertThat(response.getStatus(), equalTo(200));

        then(mockAppender).should(never()).doAppend(any());

    }

    @Test
    public void successPostRequestsShouldBeLogged() throws URISyntaxException, ExecutionException, InterruptedException, IOException {

        String url = "/postWithFilter200?param1=value1";
        WireMock.stubFor(post(urlEqualTo(url))
                .withRequestBody(equalToJson(stubReqBodyJson))
                .willReturn(okJson(stubRespBodyJson)
                        .withHeader("respHeader1", "respHV1","respHV2")));

        Map<String, Collection<String>> responseHeaders = new HashMap<>();
        responseHeaders.put("respHeader1", Arrays.asList("respHV1", "respHV2"));

        String requestMethod = HttpMethod.POST;

        Map<String, Collection<String>> headers = stubHeaders;

        ParsecAsyncHttpRequest request =
                new ParsecAsyncHttpRequest.Builder()
                        .setUrl(wireMockBaseUrl+url)
                        .setHeaders(headers)
                        .setRequestTimeout(30)
                        .setMethod(requestMethod)
                        .setBody(stubReqBodyJson).setBodyEncoding("UTF-8").build();

        Response response = parsecHttpClient.criticalExecute(request).get();

        String pattern = createLogStringPattern(requestMethod, request.getUrl(), "param1=value1",
                headers, stubReqBodyJson,
                200, responseHeaders, stubRespBodyJson);

        ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        then(mockAppender).should().doAppend(loggingEventArgumentCaptor.capture());

        String message = loggingEventArgumentCaptor.getValue().getMessage();

        assertThat(message,
                JsonMatchers.jsonEquals(pattern)
                            .whenIgnoringPaths("request.headers.Accept-Encoding",
                                    "response.headers.Content-Type","response.headers.Server","response.headers.Transfer-Encoding")

        );


        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getEntity(), is(notNullValue()));
    }

    @Test
    public void faultyPostRequestShouldBeLogged() throws URISyntaxException, JsonProcessingException {

        String url = "/postWithFilterAtFault";
        WireMock.stubFor(post(urlEqualTo(url))
                .withRequestBody(equalToJson(stubReqBodyJson))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        String requestMethod = HttpMethod.POST;



        ParsecAsyncHttpRequest request =
                new ParsecAsyncHttpRequest.Builder()
                        .setUrl(wireMockBaseUrl+url)
                        .setHeaders(stubHeaders)
                        .setRequestTimeout(30)
                        .setMethod(requestMethod)
                        .setBody(stubReqBodyJson).setBodyEncoding("UTF-8").build();

        Throwable exception = null;
        try {
            parsecHttpClient.criticalExecute(request).get();
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception, is(notNullValue()));

        ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        then(mockAppender).should().doAppend(loggingEventArgumentCaptor.capture());

        String message = loggingEventArgumentCaptor.getValue().getMessage();
        String pattern = createLogStringPatternForError(requestMethod, request.getUrl(), "",
                stubHeaders, stubReqBodyJson, null);

        assertThat(message,
                JsonMatchers.jsonEquals(pattern)
                        .whenIgnoringPaths("request.headers.Accept-Encoding")
        );
    }

    private String createLogStringPatternForError(String requestMethod, String url, String queryString,
                                                  Map<String, Collection<String>> reqHeaders, String reqBodyJson,
                                                  Object error) throws JsonProcessingException {

        Map<String, String> headers = reqHeaders.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey,
                        e -> String.join(",", e.getValue())));


        String reqHeaderString = _OBJECT_MAPPER.writeValueAsString(headers);
        String pattern = String.format("{" +
                        "\"time\":\"${json-unit.any-number}\","+
                        "\"request\": {" +
                            "\"method\": \"%s\"," +
                            "\"uri\": \"%s\"," +
                            "\"query\": \"%s\"," +
                            "\"headers\": %s," +
                            "\"payload\": \"%s\"" +
                            "}," +
                        "\"response\": {}," +
                        "\"response-error\": \"${json-unit.ignore}\", " +
                        "\"progress\": {" +
                            "\"namelookup_time\": \"${json-unit.any-number}\"," +
                            "\"connect_time\": \"${json-unit.any-number}\"," +
                            "\"pretransfer_time\": \"${json-unit.any-number}\"," +
                            "\"starttransfer_time\": \"${json-unit.any-number}\"," +
                            "\"total_time\":\"${json-unit.any-number}\"" +
                            "}" +
                        "}", requestMethod, url, queryString, reqHeaderString, StringEscapeUtils.escapeJson(reqBodyJson));

        return pattern;

    }

    private String createLogStringPattern(String requestMethod, String url, String queryString,
                                          Map<String, Collection<String>> reqHeaders, String reqBodyJson,
                                          int responseCode,
                                          Map<String, Collection<String>> respHeaders, String respBodyJson) throws JsonProcessingException {

        Map<String, String> headers = reqHeaders.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey,
                        e -> String.join(",", e.getValue())));

        String reqHeaderString = _OBJECT_MAPPER.writeValueAsString(headers);


        headers = respHeaders.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey,
                        e -> String.join(",", e.getValue())));

        String respHeaderString = _OBJECT_MAPPER.writeValueAsString(headers);

        String pattern = String.format("{" +
                "\"time\":\"${json-unit.any-number}\","+
                "\"request\": {" +
                    "\"method\": \"%s\"," +
                    "\"uri\": \"%s\"," +
                    "\"query\": \"%s\"," +
                    "\"headers\": %s," +
                    "\"payload\": \"%s\"" +
                "}," +
                "\"response\": {" +
                   "\"status\": %d," +
                   "\"headers\": %s," +
                   "\"payload\": \"%s\""+
                "}," +
                "\"progress\": {" +
                  "\"namelookup_time\": \"${json-unit.any-number}\"," +
                  "\"connect_time\": \"${json-unit.any-number}\"," +
                  "\"pretransfer_time\": \"${json-unit.any-number}\"," +
                  "\"starttransfer_time\": \"${json-unit.any-number}\"," +
                  "\"total_time\":\"${json-unit.any-number}\"" +
                  "}" +
                "}", requestMethod, url, queryString, reqHeaderString, StringEscapeUtils.escapeJson(reqBodyJson),
                     responseCode, respHeaderString, StringEscapeUtils.escapeJson(respBodyJson));

        return pattern;
    }





}
