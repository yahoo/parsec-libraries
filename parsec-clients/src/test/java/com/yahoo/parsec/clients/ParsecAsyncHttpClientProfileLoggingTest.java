// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.yahoo.parsec.test.WireMockBaseTest;
import org.mockito.ArgumentCaptor;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.matchesPattern;
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


    String profileLogTimePattern="time=\\d+.\\d+,";
    String execInfPattern = "exec_info=\\{\"namelookup_time\":\\d+,\"connect_time\":\\d+," +
            "\"pretransfer_time\":\\d+,\"starttransfer_time\":\\d+,\"total_time\":\\d+\\}";

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
                .willReturn(okJson(stubRespBodyJson)
                        .withHeader(ParsecClientDefine.HEADER_CONTENT_LENGTH, String.valueOf(stubRespBodyJson.length()))
                ));


        Map<String, Collection<String>> headers = new HashMap<>();
        headers.put(ParsecClientDefine.HEADER_HOST, Arrays.asList("nonCriticalGetHost"));


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


        //the log would look like below:
        //time=1541132036.456, req_url=http://localhost:59041/get200Profiling, req_host_header=criticalGetHost, req_method=GET, exec_info={"namelookup_time":8289,"connect_time":79202,"pretransfer_time":97919,"starttransfer_time":155815,"total_time":533641}, resp_code=200, src_url=, req_status=single, content_length=49, origin=,

        String fixedExpection1 = "req_host_header=nonCriticalGetHost, req_method=GET";
        String fixedEpxection2 = "resp_code=200, src_url=, req_status=single, content_length=49, origin=, ";

        StringBuffer msgPatternBuf = new StringBuffer("^\\[TRACE\\] ");
        msgPatternBuf.append(profileLogTimePattern)
                     .append(" req_url=").append(request.getUrl()).append(", ")
                     .append(fixedExpection1).append(", ")
                     .append(execInfPattern).append(", ")
                     .append(fixedEpxection2)
                     .append("$");

        then(mockAppender).should().doAppend(argThat(hasToString(matchesPattern(msgPatternBuf.toString()))));



    }


    @Test
    public void criticalPostRequestShouldBeLogged() throws URISyntaxException, ExecutionException, InterruptedException {

        String url = "/post200Profiling";

        WireMock.stubFor(post(urlEqualTo(url))
                .withRequestBody(equalToJson(stubReqBodyJson))
                .willReturn(okJson(stubRespBodyJson)
                        .withHeader(ParsecClientDefine.HEADER_CONTENT_LENGTH, String.valueOf(stubRespBodyJson.length()))
                ));

        String requestMethod = HttpMethod.POST;
        Map<String, Collection<String>> headers = new HashMap<>();
        headers.put(ParsecClientDefine.HEADER_HOST, Arrays.asList("criticalPostHost"));



        ParsecAsyncHttpRequest request =
                new ParsecAsyncHttpRequest.Builder()
                .setUrl(wireMockBaseUrl+url)
                .setHeaders(headers)
                .setRequestTimeout(300)
                .setMethod(requestMethod)
                .setBody(stubReqBodyJson).setBodyEncoding("UTF-8").build();


        parsecHttpClient.criticalExecute(request).get();


        String fixedExpection1 = "req_host_header=criticalPostHost, req_method=POST";
        String fixedEpxection2 = "resp_code=200, src_url=, req_status=single, content_length=49, origin=, ";

        StringBuffer msgPatternBuf = new StringBuffer("^\\[TRACE\\] ");
        msgPatternBuf.append(profileLogTimePattern)
                .append(" req_url=").append(request.getUrl()).append(", ")
                .append(fixedExpection1).append(", ")
                .append(execInfPattern).append(", ")
                .append(fixedEpxection2)
                .append("$");

        then(mockAppender).should().doAppend(argThat(hasToString(matchesPattern(msgPatternBuf.toString()))));

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
                .willReturn(okJson(stubRespBodyJson)
                        .withHeader(ParsecClientDefine.HEADER_CONTENT_LENGTH, String.valueOf(stubRespBodyJson.length()))
                ));

        String requestMethod = HttpMethod.POST;
        Map<String, Collection<String>> headers = new HashMap<>();
        headers.put(ParsecClientDefine.HEADER_HOST, Arrays.asList("postRetriesHost"));

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


        ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor =  ArgumentCaptor.forClass(ILoggingEvent.class);
        then(mockAppender).should(times(2)).doAppend(loggingEventArgumentCaptor.capture());

        String loggedFailureMsg = loggingEventArgumentCaptor.getAllValues().get(0).toString();

        String fixedExpection1 = "req_host_header=postRetriesHost, req_method=POST";
        String fiexdExpectionForFaiulre = "resp_code=500, src_url=, req_status=single, content_length=null, origin=, ";

        StringBuffer msgPatternBuf = new StringBuffer("^\\[TRACE\\] ");
        msgPatternBuf.append(profileLogTimePattern)
                .append(" req_url=").append(request.getUrl()).append(", ")
                .append(fixedExpection1).append(", ")
                .append(execInfPattern).append(", ")
                .append(fiexdExpectionForFaiulre)
                .append("$");

        assertThat(loggedFailureMsg, matchesPattern(msgPatternBuf.toString()));

        String fiexdExpectionForSuccess = "resp_code=200, src_url=, req_status=single\\|retry:500, content_length=49, origin=, ";

        msgPatternBuf = new StringBuffer("^\\[TRACE\\] ");
        msgPatternBuf.append(profileLogTimePattern)
                .append(" req_url=").append(request.getUrl()).append(", ")
                .append(fixedExpection1).append(", ")
                .append(execInfPattern).append(", ")
                .append(fiexdExpectionForSuccess)
                .append("$");

        String loggedSuccessMsg = loggingEventArgumentCaptor.getAllValues().get(1).toString();
        assertThat(loggedSuccessMsg, matchesPattern(msgPatternBuf.toString()));
    }
}
