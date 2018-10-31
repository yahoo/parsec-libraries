// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by baiyi on 10/26/2018.
 */
public class WireMockBaseTest {
    protected static WireMockServer wireMockServer;
    protected static String wireMockBaseUrl;

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

    @BeforeMethod
    public void resetWireMock() throws InterruptedException {
        WireMock.resetToDefault();
    }

    @AfterClass
    public void serverShutdown() {
        wireMockServer.stop();
    }


}
