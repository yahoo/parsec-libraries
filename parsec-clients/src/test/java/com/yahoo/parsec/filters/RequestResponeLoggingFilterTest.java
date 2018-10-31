// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.filters;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.yahoo.parsec.clients.ParsecAsyncHttpClient;
import com.yahoo.parsec.clients.ParsecAsyncHttpRequest;
import com.yahoo.parsec.test.ClientTestUtils;
import com.yahoo.parsec.test.WireMockBaseTest;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;

/**
 * Created by baiyi on 10/30/2018.
 */
public class RequestResponeLoggingFilterTest extends WireMockBaseTest {

    private static ObjectMapper _OBJECT_MAPPER = new ObjectMapper();
    private static ParsecAsyncHttpClient parsecHttpClient =
            new ParsecAsyncHttpClient.Builder()
                    .setAcceptAnyCertificate(true)
                    .addRequestFilter(new RequestResponeLoggingFilter())
                    .build();



    Appender mockAppender = mock(Appender.class);
    String reqBodyJson, respBodyJson;
    ClientTestUtils testUtils = new ClientTestUtils();



    @BeforeMethod
    public void setup() throws JsonProcessingException {
        //todo: modify the logger name later.
        Logger logger = (Logger) LoggerFactory.getLogger("parsec.clients.reqresp_log");

        mockAppender = mock(Appender.class);
        logger.addAppender(mockAppender);

        Map stubRequest = new HashMap<>();
        stubRequest.put("requestKey1", "requestValue1");
        stubRequest.put("requestKey2", "requestValue2");
        reqBodyJson = _OBJECT_MAPPER.writeValueAsString(stubRequest);

        Map stubResponse = new HashMap<>();
        stubResponse.put("respKey1", "respValue1");
        stubResponse.put("respKey2", "respValue2");
        respBodyJson = _OBJECT_MAPPER.writeValueAsString(stubResponse);

    }

    @Test
    public void successPostRequestsShouldBeLogged() throws URISyntaxException, ExecutionException, InterruptedException {

        String url = "/postWithFilter200";
        WireMock.stubFor(post(urlEqualTo(url))
                .withRequestBody(equalToJson(reqBodyJson))
                .willReturn(okJson(respBodyJson)));

        String requestMethod = HttpMethod.POST;
        ParsecAsyncHttpRequest request = testUtils.buildRequest(requestMethod,300,
                new URI(super.wireMockBaseUrl+url), new HashMap<>(), reqBodyJson);

        Response response = parsecHttpClient.criticalExecute(request).get();

        then(mockAppender).should().doAppend(argThat(hasToString(containsString(url))));

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getEntity(), is(notNullValue()));


    }
}
