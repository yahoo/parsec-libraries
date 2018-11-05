// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.web;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.TeeOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class ParsecServletResponseWrapper extends HttpServletResponseWrapper {
    final private ByteArrayOutputStream contentStream = new ByteArrayOutputStream();

    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @param response servlet response
     * @throws IllegalArgumentException if the response is null
     */
    public ParsecServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new DelegatedServletOutputStream(getResponse().getOutputStream());
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new DelegatedPrintWriter(getOutputStream());
    }

    public byte[] getContentAsByteArray() {
        return contentStream.toByteArray();
    }

    public String getContent() throws UnsupportedEncodingException {
        return contentStream.toString(getCharacterEncoding());
    }

    private class DelegatedServletOutputStream extends ServletOutputStream {

        private final ServletOutputStream servletStream;
        private final TeeOutputStream teeStream;

        DelegatedServletOutputStream(ServletOutputStream servletStream) {
            this.servletStream = servletStream;
            teeStream = new TeeOutputStream(servletStream, contentStream);
        }

        @Override
        public boolean isReady() {
            return servletStream.isReady();
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            servletStream.setWriteListener(writeListener);
        }

        @Override
        public void write(int b) throws IOException {
            teeStream.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            teeStream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            teeStream.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            teeStream.flush();
        }

        @Override
        public void close() throws IOException {
            teeStream.close();
        }
    }

    private class DelegatedPrintWriter extends PrintWriter {

        DelegatedPrintWriter(ServletOutputStream sourceStream) {
            super(new OutputStreamWriter(sourceStream));
        }

        @Override
        public void write(int c) {
            super.write(c);
            super.flush();
        }

        @Override
        public void write(String s, int off, int len) {
            super.write(s, off, len);
            super.flush();
        }

        @Override
        public void write(char[] buf, int off, int len) {
            super.write(buf, off, len);
            super.flush();
        }
    }
}
