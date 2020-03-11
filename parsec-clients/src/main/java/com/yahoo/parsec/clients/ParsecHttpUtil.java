// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.ning.http.client.Param;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.util.AsyncHttpProviderUtils;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class that helps convert Ning objects to {@link javax.ws.rs.core} objects.
 *
 * @author sho
 */
public final class ParsecHttpUtil {
    /**
     * Unused private constructor.
     */
    private ParsecHttpUtil() {

    }

    /**
     * Get {@link NewCookie} {@link List} from Ning {@link Cookie} {@link Collection}.
     *
     * @param ningCookies Ning {@link Cookie} {@link Collection}
     * @return List&lt;{@link NewCookie}&gt;
     */
    public static List<NewCookie> getCookies(final Collection<Cookie> ningCookies) {
        Stream<NewCookie> s = ningCookies.stream().map(ParsecHttpUtil::getCookie);
        return s.collect(Collectors.toList());
    }

    /**
     * Get {@link NewCookie} from Ning {@link Cookie}.
     *
     * @param ningCookie Ning {@link Cookie}
     * @return {@link NewCookie}
     */
    public static NewCookie getCookie(final Cookie ningCookie) {
        return new NewCookie(
            ningCookie.getName(),
            ningCookie.getValue(),
            ningCookie.getPath(),
            ningCookie.getDomain(),
            "",
            (int) ningCookie.getMaxAge(),
            ningCookie.isSecure(),
            ningCookie.isHttpOnly()
        );
    }

    /**
     * Get Map&lt;String, List&lt;String&gt;&gt; from Ning {@link Param} {@link List}.
     *
     * @param ningParams Ning {@link Param} {@link List}
     * @return Map&lt;String, List&lt;String&gt;&gt;
     */
    public static Map<String, List<String>> getParamsMap(final List<Param> ningParams) {
        Map<String, List<String>> params = new HashMap<>();

        for (Param ningParam : ningParams) {
            String paramKey = ningParam.getName();
            List<String> paramValues = (params.containsKey(paramKey)) ? params.get(paramKey) : new ArrayList<>();

            paramValues.add(ningParam.getValue());
            params.put(paramKey, paramValues);
        }

        return params;
    }

    /**
     * Get {@link javax.ws.rs.core} {@link Response} from Ning {@link com.ning.http.client.Response}.
     *
     * @param ningResponse Ning {@link com.ning.http.client.Response}
     * @return {@link Response}
     * @throws IOException IO exception
     */
    public static Response getResponse(final com.ning.http.client.Response ningResponse) throws IOException {
        Response.ResponseBuilder responseBuilder = Response
            .status(ningResponse.getStatusCode())
            .type(ningResponse.getContentType());

        if (ningResponse.hasResponseHeaders()) {
            ningResponse.getHeaders().entrySet()
                .forEach(entry -> entry.getValue()
                    .forEach(value -> responseBuilder.header(entry.getKey(), value)));
        }

        if (ningResponse.hasResponseBody()) {
            // we set default charset to utf8 if content type not specify
            String contentType = ningResponse.getContentType();
            String charset = parseCharsetFromContentType(contentType, StandardCharsets.UTF_8.name());
            responseBuilder.entity(ningResponse.getResponseBody(charset));
        }

        ningResponse.getCookies().forEach(ningCookie -> responseBuilder.cookie(getCookie(ningCookie)));
        return responseBuilder.build();
    }

    public static String parseCharsetFromContentType(String contentType, String defaultCharset) {
        if (contentType != null) {
            String parseCharset = AsyncHttpProviderUtils.parseCharset(contentType);
            if (parseCharset != null) {
                return parseCharset;
            }
        }
        return defaultCharset;
    }
}
