// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.web

import ch.qos.logback.classic.Logger
import groovy.json.JsonSlurper
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.DispatcherType
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RequestResponseLoggingTest extends Specification {

    @Shared
    def server, serverPort, expectedReplyContent = "{\"key\":\"val\"}"

    @Shared
    def mockAppender = new MockAppender()

    def setupSpec() {
        startServer()
        serverPort = ((ServerConnector)server.getConnectors()[0]).getLocalPort()

        def logger = (Logger)LoggerFactory.getLogger("com.yahoo.parsec.web.RequestResponseLoggingFilter")
        logger.addAppender(mockAppender)
    }

    def startServer() {
        server = new Server(0)
        def handler = new ServletHandler()
        server.setHandler(handler)

        handler.addServletWithMapping(new ServletHolder(new HelloServlet(expectedReplyContent)), "/*")
        def filterHolder = handler.addFilterWithMapping(RequestResponseLoggingFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST))
        filterHolder.setInitParameter("formatter-classname", "com.yahoo.parsec.web.JsonFormatter")

        server.start()
    }

    def setup() {
        mockAppender.setCachedMsg("") // clean buffer before test
    }

    def cleanupSpec() {
        server.stop()
        server.join()
    }

    static class HelloServlet extends HttpServlet {

        private String replyContent;

        HelloServlet(replyContent) {
            this.replyContent = replyContent
        }

        private void reply(HttpServletRequest request, HttpServletResponse response) {
            request.getReader().text
            response.setContentType("application/json")
            response.setStatus(HttpServletResponse.SC_OK)
            response.getWriter().print(replyContent)
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            reply(req, resp)
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            reply(req, resp)
        }

        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
            reply(req, resp)
        }

        @Override
        protected void doDelete(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
            reply(req, resp)
        }
    }

    def "send request with GET method should log as expected"() {
        when:
        def replyContent = "http://localhost:$serverPort/".toURL().text

        then:
        replyContent == expectedReplyContent
        mockAppender.getCachedMsg()  == ""
    }

    @Unroll
    def "send request with #HTTP_METHOD method should log as expected"() {
        given:
        def conn = new URL("http://localhost:$serverPort/").openConnection()
        def replyContent

        when:
        conn.with {
            doOutput = true
            requestMethod = HTTP_METHOD
            outputStream.withWriter { writer ->
                writer << expectedReplyContent
            }
            replyContent = content.text
        }

        then:
        replyContent == expectedReplyContent
        def auditLog = new JsonSlurper().parseText(mockAppender.getCachedMsg())
        auditLog.request.payload == expectedReplyContent
        auditLog.response.payload == expectedReplyContent

        where:
        HTTP_METHOD << ["POST", "PUT", "DELETE"]
    }
}
