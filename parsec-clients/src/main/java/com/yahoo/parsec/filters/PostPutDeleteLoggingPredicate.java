package com.yahoo.parsec.filters;

import com.ning.http.client.Request;
import com.ning.http.client.Response;

import javax.ws.rs.HttpMethod;
import java.util.function.BiPredicate;

/**
 * Only log request and response if the request method is one of POST, PUT, or DELETE
 * Created by baiyi on 10/31/2018.
 */
public class PostPutDeleteLoggingPredicate implements BiPredicate<Request, Response> {
    @Override
    public boolean test(Request request, Response response) {
        return (request.getMethod().equals(HttpMethod.POST)
                || request.getMethod().equals(HttpMethod.PUT)
                || request.getMethod().equals(HttpMethod.DELETE));
    }
}
