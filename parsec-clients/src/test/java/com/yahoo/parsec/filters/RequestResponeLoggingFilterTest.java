// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.yahoo.parsec.clients.ParsecAsyncHttpClient;
import com.yahoo.parsec.clients.ParsecAsyncHttpRequest;
import com.yahoo.parsec.clients.ParsecClientDefine;
import com.yahoo.parsec.test.WireMockBaseTest;
import org.mockito.ArgumentCaptor;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

/**
 * Created by baiyi on 10/30/2018.
 */
public class RequestResponeLoggingFilterTest extends WireMockBaseTest {


    private ParsecAsyncHttpClient parsecHttpClient;
    NingRequestResponseFormatter mockFormatter;

    Map<String, Collection<String>> stubHeaders;

    @BeforeTest
    public void setupCommon() throws JsonProcessingException {
        stubHeaders = new HashMap<>();
        stubHeaders.put("header1", Arrays.asList("value1"));
        stubHeaders.put("header2", Arrays.asList("value2"));

    }


    @BeforeMethod
    public void setup() {
        mockFormatter = mock(NingRequestResponseFormatter.class);
        parsecHttpClient = new ParsecAsyncHttpClient.Builder()
                .setAcceptAnyCertificate(true)
                .addRequestFilter(new RequestResponeLoggingFilter(mockFormatter))
                .build();
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

        then(mockFormatter).should(never()).format(any(), any(), any());
        assertThat(response.getStatus(), equalTo(200));
    }

    @Test
    public void successPostRequestsShouldBeLogged() throws URISyntaxException, ExecutionException, InterruptedException, IOException {

        String url = "/postWithFilter200?param1=value1";
        WireMock.stubFor(post(urlEqualTo(url))
                .withRequestBody(equalToJson(stubReqBodyJson))
                .willReturn(okJson(stubRespBodyJson)));

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


        ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);

        then(mockFormatter).should()
                .format(argThat(allOf(hasProperty("stringData", equalTo(stubReqBodyJson)),
                                      hasProperty("url", equalTo(request.getUrl())),
                                      hasProperty("headers", hasKey("header1"))
                                      )
                                ), // request
                        argThat(allOf(hasProperty("responseBody", equalTo(stubRespBodyJson)),
                                      hasProperty("statusCode", equalTo(200))
                                     )
                                ), //response
                        mapArgumentCaptor.capture());


        Map additionalArgs = mapArgumentCaptor.getValue();
        assertThat(additionalArgs.get(ParsecClientDefine.PROFILING_ASYNC_PROGRESS), is(notNullValue()));

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getEntity(), is(notNullValue()));
    }

    @Test
    public void faultyPostRequestShouldBeLogged() throws URISyntaxException {

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

        ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);


        then(mockFormatter).should()
                .format(argThat(allOf(hasProperty("stringData", equalTo(stubReqBodyJson)),
                                      hasProperty("url", equalTo(request.getUrl())),
                                      hasProperty("headers", hasKey("header1"))
                                     )
                                ), // request
                        argThat(nullValue(com.ning.http.client.Response.class)),  //response
                        mapArgumentCaptor.capture());


        Map additionalArgs = mapArgumentCaptor.getValue();
        assertThat(additionalArgs.get(ParsecClientDefine.PROFILING_ASYNC_PROGRESS), is(notNullValue()));
        assertThat(additionalArgs.get(ParsecClientDefine.RESPONSE_ERROR), is(notNullValue()));

    }




}
