package com.yahoo.parsec.clients;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Created by baiyi on 10/26/2018.
 */
public class WireMockBaseTest {
    protected WireMockServer wireMockServer;
    protected String wireMockBaseUrl;


    @BeforeTest
    public void setupServer() throws IOException {
        String rootDirPath = "parsec_wiremock_test";
        File root = Files.createTempDirectory(rootDirPath).toFile();
        new File(root, MAPPINGS_ROOT).mkdirs();
        new File(root, FILES_ROOT).mkdirs();
        wireMockServer = new WireMockServer(
                options()
                        .port(8080)
                        .fileSource(new SingleRootFileSource(rootDirPath))
        );

        wireMockServer.start();
        wireMockBaseUrl = "http://localhost:"+wireMockServer.port();

    }

    @BeforeMethod
    public void init() throws InterruptedException {
        new WireMock(wireMockServer.port()).resetMappings();
    }

    @AfterTest
    public void serverShutdown() {
        wireMockServer.stop();
    }


}
