// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.web;


import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

public class ParsecServletRequestWrapper extends HttpServletRequestWrapper {

    final private ByteArrayOutputStream contentStream;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request servlet request
     */
    public ParsecServletRequestWrapper(HttpServletRequest request) {
        super(request);
        int contentLength = request.getContentLength();
        contentStream = new ByteArrayOutputStream(contentLength >= 0 ? contentLength : 1024);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new DelegatedInputStream(getRequest().getInputStream());
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    public byte[] getContentAsByteArray() {
        return contentStream.toByteArray();
    }

    public String getContent() throws UnsupportedEncodingException {
        String charset = Optional.ofNullable(getCharacterEncoding()).orElse("UTF-8");
        return contentStream.toString(charset);
    }

    private class DelegatedInputStream extends ServletInputStream {
        private final ServletInputStream servletStream;
        private final TeeInputStream teeStream;

        DelegatedInputStream(ServletInputStream servletStream) {
            this.servletStream = servletStream;
            this.teeStream = new TeeInputStream(servletStream, contentStream);
        }

        @Override
        public int read() throws IOException {
            return teeStream.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return teeStream.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return teeStream.read(b, off, len);
        }

        @Override
        public boolean isFinished() {
            return servletStream.isFinished();
        }

        @Override
        public boolean isReady() {
            return servletStream.isReady();
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            servletStream.setReadListener(readListener);
        }
    }
}
