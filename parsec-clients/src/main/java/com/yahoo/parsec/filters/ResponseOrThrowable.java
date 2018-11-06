package com.yahoo.parsec.filters;


import com.ning.http.client.Response;

/**
 * Created by baiyi on 11/05/2018.
 */
public class ResponseOrThrowable {

    private final Response response;
    private final Throwable throwable;

    public ResponseOrThrowable(Response response, Throwable throwable) {
        this.response = response;
        this.throwable = throwable;
    }

    public Response getResponse() {
        return response;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
