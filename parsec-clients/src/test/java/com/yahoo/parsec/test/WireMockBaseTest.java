// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by baiyi on 10/26/2018.
 */
public class WireMockBaseTest {

    private static ObjectMapper _OBJECT_MAPPER = new ObjectMapper();
    protected static WireMockServer wireMockServer;
    protected static String wireMockBaseUrl;
    protected static String stubReqBodyJson, stubRespBodyJson;

    @BeforeClass
    public static void setupServer() throws IOException {
        String rootDirPath = "parsec_wiremock_test";
        File root = Files.createTempDirectory(rootDirPath).toFile();
        new File(root, WireMockApp.MAPPINGS_ROOT).mkdirs();
        new File(root, WireMockApp.FILES_ROOT).mkdirs();
        wireMockServer = new WireMockServer(
                WireMockConfiguration.options()
                        .dynamicPort()
                        .fileSource(new SingleRootFileSource(rootDirPath))
        );

        wireMockServer.start();
        wireMockBaseUrl = "http://localhost:"+wireMockServer.port();
        WireMock.configureFor(wireMockServer.port());
    }

    @BeforeClass
    public static void createWireMockStubReqAndResp() throws JsonProcessingException {

        Map stubRequest = new HashMap<>();
        stubRequest.put("requestKey1", "requestValue1");
        stubRequest.put("requestKey2", "requestValue2");
        stubReqBodyJson = _OBJECT_MAPPER.writeValueAsString(stubRequest);

        Map stubResponse = new HashMap<>();
        stubResponse.put("respKey1", "respValue1");
        stubResponse.put("respKey2", "respValue2");
        stubRespBodyJson = _OBJECT_MAPPER.writeValueAsString(stubResponse);
    }

    @BeforeMethod
    public void resetWireMock() throws InterruptedException {
        WireMock.resetToDefault();
    }



    @AfterClass
    public void serverShutdown() {
        wireMockServer.stop();
    }


}
