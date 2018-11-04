// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.google.common.base.Preconditions;
import com.ning.http.client.*;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.client.multipart.ByteArrayPart;
import com.ning.http.client.multipart.FilePart;
import com.ning.http.client.multipart.Part;
import com.ning.http.client.multipart.StringPart;
import com.ning.http.client.uri.Uri;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;


/**
 * Request class representing HTTP request.
 *
 * @author sho
 */
public class ParsecAsyncHttpRequest {
    /**
     * Flag for cirtical get.
     */
    private final boolean criticalGet;

    /**
     * Max retries.
     */
    private final int maxRetries;

    /**
     * Response status codes to retry.
     */
    private final List<Integer> retryStatusCodes;

    /**
     * Exceptions to retry.
     */
    private final List<Class<? extends Throwable>> retryExceptions;

    /**
     * Cookies.
     */
    private final List<NewCookie> cookies;

    /**
     * Form params.
     */
    private final Map<String, List<String>> formParams;

    /**
     * Query params.
     */
    private final Map<String, List<String>> queryParams;

    /**
     * Headers.
     */
    private final Map<String, List<String>> headers;

    /**
     * Ning request.
     */
    private final Request ningRequest;

    /**
     * Flag to append Accept-Encoding: deflate header.
     */
    private final boolean acceptCompression;

    /**
     * Unused constructor.
     */
    @SuppressWarnings("unused")
    ParsecAsyncHttpRequest() {
        throw new UnsupportedOperationException();
    }

    /**
     * Private constructor.
     * @param builder builder
     */
    private ParsecAsyncHttpRequest(final Builder builder) {
        criticalGet = builder.criticalGet;
        maxRetries = builder.maxRetries;
        retryStatusCodes = builder.retryStatusCodes;
        retryExceptions = builder.retryExceptions;
        ningRequest = builder.ningRequestBuilder.build();
        acceptCompression = builder.acceptCompression;

        headers = new FluentCaseInsensitiveStringsMap(ningRequest.getHeaders());
        cookies = ParsecHttpUtil.getCookies(ningRequest.getCookies());
        formParams = ParsecHttpUtil.getParamsMap(ningRequest.getFormParams());
        queryParams = ParsecHttpUtil.getParamsMap(ningRequest.getQueryParams());
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof ParsecAsyncHttpRequest)) {
            return false;
        }

        // Compare local fields except Ning request
        if (!EqualsBuilder.reflectionEquals(this, object, new String[] {"ningRequest"})) {
            return false;
        }

        // Compare Ning request fields
        return ParsecEqualsUtil.ningRequestEquals(ningRequest, ((ParsecAsyncHttpRequest) object).ningRequest);
    }

    @Override
    public int hashCode() {
        // Calculate Ning request hash by custom algorithm
        int ningRequestHash = new HashCodeBuilder()
            .append(ningRequest.getBodyEncoding())
            .append(ningRequest.getContentLength())
            .append(ningRequest.getFollowRedirect())
            .append(ningRequest.getMethod())
            .append(ningRequest.getRangeOffset())
            .append(ningRequest.getRequestTimeout())
            .append(ningRequest.getStringData())
            .append(ningRequest.getUrl())
            .append(ningRequest.getVirtualHost())
            .hashCode();

        return HashCodeBuilder.reflectionHashCode(this, new String[] {"ningRequest"}) + ningRequestHash;
    }

    /**
     * Get body.
     *
     * @return Body
     */
    public String getBody() {
        return ningRequest.getStringData();
    }

    /**
     * Get body.
     *
     * @return Body
     */
    public byte[] getBodyByte() {
        return ningRequest.getByteData();
    }

    /**
     * Get body encoding.
     *
     * @return Body encoding
     */
    public String getBodyEncoding() {
        return ningRequest.getBodyEncoding();
    }

    /**
     * Get content length.
     *
     * @return Content length
     */
    public long getContentLength() {
        return ningRequest.getContentLength();
    }

    /**
     * Get cookies.
     *
     * @return Cookies
     */
    public Collection<NewCookie> getCookies() {
        return Collections.unmodifiableCollection(cookies);
    }

    /**
     * Get form parameters.
     *
     * @return Form parameters
     */
    public Map<String, List<String>> getFormParams() {
        return Collections.unmodifiableMap(formParams);
    }

    /**
     * Get header string.
     *
     * @param headerKey Header key to get
     * @return Header value
     */
    public String getHeaderString(String headerKey) {
        if (headers.containsKey(headerKey)) {
            String arrayString = headers.get(headerKey).toString();
            return arrayString.substring(1, arrayString.length() - 1);
        }
        return null;
    }

    /**
     * Get headers.
     *
     * @return Headers
     */
    public Map<String, List<String>> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    /**
     * Get max retries.
     *
     * @return Max retries
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Get method.
     *
     * @return Method
     */
    public String getMethod() {
        return ningRequest.getMethod();
    }

    /**
     * Get Ning {@link Request}.
     *
     * @return Ning {@link Request}
     */
    Request getNingRequest() {
        return ningRequest;
    }

    /**
     * Get proxy server.
     *
     * @return Proxy server
     */
    public String getProxyServer() {
        ProxyServer proxyServer = ningRequest.getProxyServer();
        if (proxyServer == null) {
            return null;
        }
        return proxyServer.toString();
    }

    /**
     * Get query parameters.
     *
     * @return Query parameters
     */
    public Map<String, List<String>> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    /**
     * Get range offset.
     *
     * @return Range offset
     */
    public long getRangeOffset() {
        return ningRequest.getRangeOffset();
    }

    /**
     * Get request timeout.
     *
     * @return Request timeout
     */
    public int getRequestTimeout() {
        return ningRequest.getRequestTimeout();
    }

    /**
     * Get retry status codes.
     *
     * @return List of retry status codes
     */
    public List<Integer> getRetryStatusCodes() {
        return Collections.unmodifiableList(retryStatusCodes);
    }

    /**
     * Get retry exceptions.
     *
     * @return List of retry exceptions
     */
    public List<Class<? extends Throwable>> getRetryExceptions() {
        return Collections.unmodifiableList(retryExceptions);
    }

    /**
     * Get URI.
     *
     * @return {@link URI}
     * @throws URISyntaxException Uri syntax exception
     */
    public URI getUri() throws URISyntaxException {
        return ningRequest.getUri().toJavaNetURI();
    }

    /**
     * Get url.
     *
     * @return Url
     */
    public String getUrl() {
        return ningRequest.getUrl();
    }

    /**
     * Get virtual host.
     *
     * @return Virtual host
     */
    public String getVirtualHost() {
        return ningRequest.getVirtualHost();
    }


    /**
     * Is critical get.
     *
     * @return Whether request is critical get
     */
    public boolean isCriticalGet() {
        return criticalGet;
    }

    /**
     * Is follow redirect.
     *
     * @return Whether request will follow redirect
     */
    public boolean isFollowRedirect() {
        return ningRequest.getFollowRedirect();
    }

    /**
     * Is accept compression.
     *
     * @return Whether request will accept compression
     */
    public boolean isAcceptCompression() {
        return acceptCompression;
    }

    /**
     * Gets the name resolver.
     *
     * @return Name resolver.
     */
    public ParsecNameResolver getNameResolver() {
        return ((DelegateNameResolver) ningRequest.getNameResolver()).getDelegate();
    }

    /**
     * Static Builder class for {@link ParsecAsyncHttpRequest}.
     *
     * @author sho
     */
    public static class Builder {
        /**
         * Default max retries.
         */
        private static final int DEFAULT_MAX_RETRIES = 3;

        /**
         * Accept encoding header.
         */
        private static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";

        /**
         * Encoding type.
         */
        private static final String COMPRESSION_TYPE = "deflate";

        /**
         * Flag for critical get.
         */
        private boolean criticalGet;

        /**
         * Flag for follow redirect.
         */
        private boolean followRedirect;

        /**
         * Headers.
         */
        private FluentCaseInsensitiveStringsMap headers;

        /**
         * Content length.
         */
        private int contentLength;

        /**
         * Max retries.
         */
        private int maxRetries;

        /**
         * Request timeout.
         */
        private int requestTimeout;

        /**
         * Cookies.
         */
        private List<NewCookie> cookies;

        /**
         * Response status codes to retry.
         */
        private List<Integer> retryStatusCodes;

        /**
         * Exceptions to retry.
         */
        private List<Class<? extends Throwable>> retryExceptions;

        /**
         * Query params.
         */
        private List<Param> queryParams;

        /**
         * Form params.
         */
        private List<Param> formParams;

        /**
         * Range offset.
         */
        private long rangeOffset;

        /**
         * Proxy server.
         */
        private ProxyServer proxyServer;

        /**
         * Ning request builder.
         */
        private RequestBuilder ningRequestBuilder;

        /**
         * Body.
         */
        private String body;

        /**
         * Body for binary.
         */
        private byte[] byteBody;

        /**
         * Body encoding.
         */
        private String bodyEncoding;

        /**
         * Method.
         */
        private String method;

        /**
         * Virtual host.
         */
        private String virtualHost;

        /**
         * Uri.
         */
        private URI uri;

        /**
         * Flag to append Accept-Encoding: deflate header.
         */
        private boolean acceptCompression;

        /**
         * Body parts.
         */
        private List<Part> bodyParts;

        /**
         * DNS name resolver.
         */
        private ParsecNameResolver nameResolver;

        /**
         * Constructor.
         */
        public Builder() {
            // Init default values
            method = "GET";
            maxRetries = DEFAULT_MAX_RETRIES;
            headers = new FluentCaseInsensitiveStringsMap();
            retryStatusCodes = new ArrayList<>();
            retryExceptions = new ArrayList<>();
            uri = URI.create("http://localhost");
            acceptCompression = true;
            bodyParts = new ArrayList<>();
            nameResolver = StandardNameResolver.getInstance();
        }

        /**
         * Add cookie.
         *
         * @param cookie Cookie
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder addCookie(NewCookie cookie) {
            if (cookies == null) {
                cookies = new ArrayList<>(1);
            }
            cookies.add(cookie);
            return this;
        }

        /**
         * Add form parameter.
         *
         * @param key Form key
         * @param value Form value
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder addFormParam(String key, String value) {
            if (formParams == null) {
                formParams = new ArrayList<>(1);
            }
            formParams.add(new Param(key, value));
            body = null;
            return this;
        }

        /**
         * Add header.
         *
         * @param name Header name
         * @param value Header value
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder addHeader(String name, String value) {
            headers.add(name, value);
            return this;
        }

        /**
         * Add query parameter.
         *
         * @param name Query name
         * @param value Query value
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder addQueryParam(String name, String value) {
            if (queryParams == null) {
                queryParams = new ArrayList<>(1);
            }
            queryParams.add(new Param(name, value));
            return this;
        }

        /**
         * Add retry status code.
         *
         * @param statusCode Retry status code to add
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder addRetryStatusCode(int statusCode) {
            if (Response.Status.fromStatusCode(statusCode) != Response.Status.OK
                && !retryStatusCodes.contains(statusCode)) {
                retryStatusCodes.add(statusCode);
            }
            return this;
        }

        /**
         * Add retry exception.
         *
         * @param exception Retry exception to add
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder addRetryException(Class<? extends Exception> exception) {
            if (!retryExceptions.contains(exception)) {
                retryExceptions.add(exception);
            }
            return this;
        }

        /**
         * add body part for multipart/form-data upload.
         * @param name part name
         * @param value part value
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder addBodyPart(String name, String value) {
            Part part = new StringPart(name, value);
            bodyParts.add(part);
            return this;
        }

        /**
         * add body part for multipart/form-data upload.
         * @param name part name
         * @param value part value
         * @param contentType content type
         * @param charset content charset
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder addBodyPart(String name, String value, String contentType, Charset charset) {
            Part part = new StringPart(name, value, contentType, charset);
            bodyParts.add(part);
            return this;
        }

        /**
         * add body part for multipart/form-data upload.
         * @param name part name
         * @param file part file data
         * @param contentType content type
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder addBodyPart(String name, File file, String contentType) {
            Part part = new FilePart(name, file, contentType);
            bodyParts.add(part);
            return this;
        }

        /**
         * add body part for multipart/form-data upload.
         * @param name part name
         * @param bytes part file data
         * @param contentType content type
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder addBodyPart(String name, byte[] bytes, String contentType) {
            Part part = new ByteArrayPart(name, bytes, contentType);
            bodyParts.add(part);
            return this;
        }

        /**
         * Buile new {@link ParsecAsyncHttpRequest} instance.
         *
         * @return {@link ParsecAsyncHttpRequest}
         */
        @SuppressWarnings("PMD.NPathComplexity")
        public ParsecAsyncHttpRequest build() {
            ningRequestBuilder = new RequestBuilder(method)
                .setContentLength(contentLength)
                .setFollowRedirects(followRedirect)
                .setHeaders(headers)
                .setProxyServer(proxyServer)
                .setRangeOffset(rangeOffset)
                .setRequestTimeout(requestTimeout)
                .setVirtualHost(virtualHost)
                .setUri(new Uri(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort(),
                    uri.getPath(),
                    uri.getRawQuery()
            ));

            if (body != null) {
                ningRequestBuilder.setBody(body);
            } else if (byteBody != null) {
                ningRequestBuilder.setBody(byteBody);
            }

            if (bodyEncoding != null) {
                ningRequestBuilder.setBodyEncoding(bodyEncoding);
            }

            if (cookies != null && !cookies.isEmpty()) {
                for (NewCookie cookie : cookies) {
                    ningRequestBuilder.addCookie(new Cookie(
                        cookie.getName(),
                        cookie.getValue(),
                        false,
                        cookie.getDomain(),
                        cookie.getPath(),
                        cookie.getMaxAge(),
                        cookie.isSecure(),
                        cookie.isHttpOnly()
                    ));
                }
            }

            if (formParams != null) {
                ningRequestBuilder.setFormParams(formParams);
            }

            if (queryParams != null) {
                ningRequestBuilder.setQueryParams(queryParams);
            }

            if (acceptCompression && !headers.containsKey(ACCEPT_ENCODING_HEADER)) {
                ningRequestBuilder.addHeader(ACCEPT_ENCODING_HEADER, COMPRESSION_TYPE);
            }

            for (Part part: bodyParts) {
                ningRequestBuilder.addBodyPart(part);
            }

            ningRequestBuilder.setNameResolver(new DelegateNameResolver(nameResolver));

            return new ParsecAsyncHttpRequest(this);
        }

        /**
         * Get {@link Param} {@link List} from {@literal Map<String, List<String>> map}.
         * @param map Parameters
         * @return List&lt;{@link Param}&gt;
         */
        private List<Param> mapToParamList(Map<String, List<String>> map) {
            if (map == null) {
                return null;
            }

            List<Param> params = new ArrayList<>(map.size());
            map.entrySet().stream()
                    .forEach(entry -> entry.getValue().stream()
                            .forEach(value -> params.add(new Param(entry.getKey(), value))));

            return params;
        }

        /**
         * Remove retry status code.
         *
         * @param statusCode Retry status code to remove
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder removeRetryStatusCode(int statusCode) {
            retryStatusCodes.remove((Integer) statusCode);
            return this;
        }

        /**
         * Remove retry exception.
         *
         * @param exception Retry exception to remove
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder removeRetryException(Class<? extends Throwable> exception) {
            retryExceptions.remove(exception);
            return this;
        }

        /**
         * Set body.
         *
         * @param body Body
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setBody(String body) {
            this.body = body;
            formParams = null;
            this.byteBody = null;
            return this;
        }

        /**
         * Set body.
         *
         * @param body Body
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setBody(byte[] body) {
            this.byteBody = Arrays.copyOf(body, body.length);
            formParams = null;
            this.body = null;
            return this;
        }

        /**
         * Set body encoding.
         *
         * @param bodyEncoding Body encoding
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setBodyEncoding(String bodyEncoding) {
            this.bodyEncoding = bodyEncoding;
            return this;
        }

        /**
         * Set content length.
         *
         * @param contentLength Content length
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setContentLength(int contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        /**
         * Set cookies.
         *
         * @param cookies {@link NewCookie} {@link List}
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setCookies(List<NewCookie> cookies) {
            this.cookies = cookies;
            return this;
        }

        /**
         * Set critical get.
         *
         * @param criticalGet Critical get
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setCriticalGet(boolean criticalGet) {
            this.criticalGet = criticalGet;
            return this;
        }

        /**
         * Set follow redirects.
         *
         * @param followRedirects Follow redirects
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setFollowRedirects(boolean followRedirects) {
            this.followRedirect = followRedirects;
            return this;
        }

        /**
         * Set form paramemters.
         *
         * @param params Form parameters
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setFormParams(Map<String, List<String>> params) {
            formParams = mapToParamList(params);
            return this;
        }

        /**
         * Set header.
         *
         * @param name Header name
         * @param value Header value
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setHeader(String name, String value) {
            headers.replaceWith(name, value);
            return this;
        }

        /**
         * Set headers.
         *
         * @param headers Headers
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setHeaders(Map<String, Collection<String>> headers) {
            if (headers == null) {
                this.headers.clear();
            } else {
                this.headers = new FluentCaseInsensitiveStringsMap(headers);
            }
            return this;
        }

        /**
         * Set max retries.
         *
         * @param maxRetries Max retries
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Set method.
         *
         * @param method Method
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setMethod(String method) {
            this.method = method;
            return this;
        }

        /**
         * Set proxy server.
         *
         * @param protocol {@link ParsecNetProtocol} protocol
         * @param host Host
         * @param port port
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setProxyServer(ParsecNetProtocol protocol, String host, int port) {
            if (protocol == ParsecNetProtocol.HTTPS) {
                proxyServer = new ProxyServer(ProxyServer.Protocol.HTTPS, host, port);
            } else {
                proxyServer = new ProxyServer(ProxyServer.Protocol.HTTP, host, port);
            }
            return this;
        }

        /**
         * Set query parameters.
         *
         * @param params Query parameters
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setQueryParams(Map<String, List<String>> params) {
            queryParams = mapToParamList(params);
            return this;
        }

        /**
         * Set range offset.
         *
         * @param rangeOffset Range offset
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setRangeOffset(long rangeOffset) {
            this.rangeOffset = rangeOffset;
            return this;
        }

        /**
         * Set request timeout.
         *
         * @param requestTimeout Request timeout
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setRequestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        /**
         * Set URI.
         *
         * @param uri {@link URI}
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setUri(URI uri) {
            this.uri = uri;
            return this;
        }

        /**
         * Set url.
         *
         * @param url Url
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setUrl(String url) {
            this.uri = URI.create(url);
            return this;
        }

        /**
         * Set virtual host.
         *
         * @param virtualHost Virtual host
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setVirtualHost(String virtualHost) {
            this.virtualHost = virtualHost;
            return this;
        }

        /**
         * Set accept compression.
         *
         * @param acceptCompression enable accept compression
         * @return {@link ParsecAsyncHttpRequest.Builder}
         */
        public Builder setAcceptCompression(boolean acceptCompression) {
            this.acceptCompression = acceptCompression;
            return this;
        }

        public Builder setNameResolver(ParsecNameResolver nameResolver) {
            Preconditions.checkNotNull(nameResolver, "Name resolver cannot be null");
            this.nameResolver = nameResolver;
            return this;
        }
    }
}
