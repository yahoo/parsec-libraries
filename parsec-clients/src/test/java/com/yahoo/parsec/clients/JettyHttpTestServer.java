// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.MultiPartInputStreamParser;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.concurrent.TimeoutException;


/**
 * Jetty HTTP test server
 *
 * @author sho
 */
public class JettyHttpTestServer {
    private int port;
	private Server server;
    private String host;

    public JettyHttpTestServer() {
        this("localhost", 8888);
    }

	public JettyHttpTestServer(String host, int port){
        this.host = host;
        this.port = port;
		server = new Server();

        ServerConnector http = new ServerConnector(server);
        http.setHost(host);
        http.setPort(port);
        http.setIdleTimeout(30000);

        server.addConnector(http);
        server.setHandler(new RequestHandler());

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.port = http.getLocalPort();
	}
	
    public String getHost() {
        return host;
    }

	public int getPort() {
        return port;
	}
	
	public void stop(){
		try {
			server.stop();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class RequestHandler extends AbstractHandler {

        private static final String MULTIPART_FORMDATA_TYPE = "multipart/form-data";

		public RequestHandler() {
			super();
		}

        private boolean isMultipartRequest(ServletRequest request) {
            return request.getContentType() != null
                    && request.getContentType().startsWith(MULTIPART_FORMDATA_TYPE);
        }

		public void handle(
            final String target,
            final Request baseRequest,
            final HttpServletRequest request,
            final HttpServletResponse response) throws IOException, ServletException {

            boolean multipartRequest = isMultipartRequest(request);
            if (multipartRequest) {
                baseRequest.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, new MultipartConfigElement("/tmp"));
            }

            int statusCode = 0;

            String path = target;
            if (path.startsWith("/sleep/")) {
                path = path.substring("/sleep/".length());
                Integer millis = 0;
                try {
                    millis = Integer.parseInt(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(millis);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                statusCode = 200;
            } else if (path.startsWith("/")) {
                path = path.substring("/".length());
                try {
                    statusCode = Integer.parseInt(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            response.setStatus(statusCode);
            response.setHeader("Host", host);
            response.setHeader("ServerPort", Integer.toString(request.getServerPort()));
            response.setHeader("SocketPort", Integer.toString(port));

            if (multipartRequest) {
                Collection<Part> parts = request.getParts();
                for (Part part : parts) {
                    String headerName = part.getName();
                    MultiPartInputStreamParser.MultiPart multiPart = ((MultiPartInputStreamParser.MultiPart)part);
                    if (multiPart.getHeader("Content-Type").startsWith("text/plain")) {
                        String headerVal = new String(multiPart.getBytes());
                        response.setHeader(headerName, headerVal);
                    }
                }
            }
            Writer writer = response.getWriter();
            writer.flush();

            if (multipartRequest) {
                MultiPartInputStreamParser multipartInputStream = (MultiPartInputStreamParser) request
                        .getAttribute(Request.__MULTIPART_INPUT_STREAM);
                if (multipartInputStream != null) {
                    try {
                        multipartInputStream.deleteParts();
                    } catch (MultiException e) {
                        e.printStackTrace();
                    }
                }
            }
		}
	}
}