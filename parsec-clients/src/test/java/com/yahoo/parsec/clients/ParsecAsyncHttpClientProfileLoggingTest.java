package com.yahoo.parsec.clients;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;

/**
 * Created by baiyi on 10/26/2018.
 */
public class ParsecAsyncHttpClientProfileLoggingTest extends WireMockBaseTest {

    static {
        System.setProperty("logback.configurationFile", "src/test/resources/logback-enableProfiling.xml");
    }

    private static ObjectMapper _OBJECT_MAPPER = new ObjectMapper();
    private static  ParsecAsyncHttpClient parsecHttpClient =
            new ParsecAsyncHttpClient.Builder().setAcceptAnyCertificate(true).build();



    Appender mockAppender = mock(Appender.class);
    String reqBodyJson, respBodyJson;

    @BeforeMethod
    public void setup() throws JsonProcessingException {
        Logger logger = (Logger) LoggerFactory.getLogger("parsec.clients.profiling_log");
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
    public void nonCriticalGetShouldBeLogged() throws URISyntaxException, ExecutionException, InterruptedException {

        String url = "/get200";
        stubFor(get(urlEqualTo(url))
                .willReturn(okJson(respBodyJson)));


        String requestMethod = "GET";
        ParsecAsyncHttpRequest request = buildRequest(requestMethod,30000,
                new URI(super.wireMockBaseUrl+url), new HashMap<>(), reqBodyJson);

        Response response = parsecHttpClient.execute(request).get();

        String expectedSubString = "req_url=http://localhost:8080/get200";

        assertThat(response.getStatus(), equalTo(200));
        then(mockAppender).should().doAppend(argThat(hasToString(containsString(expectedSubString))));
    }

    @Test
    public void testLoggingForPost() throws URISyntaxException, ExecutionException, InterruptedException {

        String url = "/post200";
        stubFor(post(urlEqualTo(url))
                .withRequestBody(equalToJson(reqBodyJson))
                .willReturn(okJson(respBodyJson)));

        String requestMethod = "POST";
        ParsecAsyncHttpRequest request = buildRequest(requestMethod,300,
                new URI(super.wireMockBaseUrl+url), new HashMap<>(), reqBodyJson);

        Response response = parsecHttpClient.criticalExecute(request).get();

        String expectedSubString = "req_url=http://localhost:8080/post200";


        assertThat(response.getStatus(), equalTo(200));
        then(mockAppender).should().doAppend(argThat(hasToString(containsString(expectedSubString))));

    }


    ParsecAsyncHttpRequest buildRequest(String method, int requestTimeout, URI uri,
                                        Map<String, List<String>> headers,
                                        String bodyAsJson) {

        ParsecAsyncHttpRequest.Builder builder = new ParsecAsyncHttpRequest.Builder();

        builder.setUri(uri);
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String headerKey = entry.getKey();
                for (String headerValue : entry.getValue()) {
                    builder.addHeader(headerKey, headerValue);
                }
            }
        }

        builder.setRequestTimeout(requestTimeout);

        builder.setMethod(method);
        builder.setBody(bodyAsJson).setBodyEncoding("UTF-8");
        return builder.build();
    }
}
