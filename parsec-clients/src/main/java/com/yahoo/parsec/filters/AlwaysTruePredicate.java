package com.yahoo.parsec.filters;

import com.ning.http.client.Request;

import java.util.function.BiPredicate;

/**
 * Created by baiyi on 11/05/2018.
 */
public class AlwaysTruePredicate implements BiPredicate<Request, ResponseOrThrowable> {
    @Override
    public boolean test(Request request, ResponseOrThrowable responseOrThrowable) {
        return true;
    }
}
