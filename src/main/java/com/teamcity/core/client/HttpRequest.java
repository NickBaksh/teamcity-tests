package com.teamcity.core.client;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
public final class HttpRequest {

    public enum Method {
        GET, POST, PUT, DELETE, PATCH
    }

    private final Method method;
    private final String endpoint;
    private final Object body;
    private final Map<String, String> pathParams;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;
    private final ContentType contentType;

    private HttpRequest(Builder builder) {
        this.method = builder.method;
        this.endpoint = builder.endpoint;
        this.body = builder.body;
        this.pathParams = Collections.unmodifiableMap(new HashMap<>(builder.pathParams));
        this.queryParams = Collections.unmodifiableMap(new HashMap<>(builder.queryParams));
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
        this.contentType = builder.contentType;
    }

    public static Builder get(String endpoint) {
        return new Builder(Method.GET, endpoint);
    }

    public static Builder post(String endpoint) {
        return new Builder(Method.POST, endpoint);
    }

    public static Builder put(String endpoint) {
        return new Builder(Method.PUT, endpoint);
    }

    public static Builder delete(String endpoint) {
        return new Builder(Method.DELETE, endpoint);
    }

    public static Builder patch(String endpoint) {
        return new Builder(Method.PATCH, endpoint);
    }

    public static final class Builder {
        private final Method method;
        private final String endpoint;
        private Object body;
        private final Map<String, String> pathParams = new HashMap<>();
        private final Map<String, String> queryParams = new HashMap<>();
        private final Map<String, String> headers = new HashMap<>();
        private ContentType contentType = ContentType.JSON;

        private Builder(Method method, String endpoint) {
            this.method = method;
            this.endpoint = endpoint;
        }

        public Builder body(Object body) {
            this.body = body;
            return this;
        }

        public Builder pathParam(String name, Object value) {
            this.pathParams.put(name, String.valueOf(value));
            return this;
        }

        public Builder pathParams(Map<String, String> params) {
            if (params != null) {
                this.pathParams.putAll(params);
            }
            return this;
        }

        public Builder queryParam(String name, Object value) {
            this.queryParams.put(name, String.valueOf(value));
            return this;
        }

        public Builder queryParams(Map<String, String> params) {
            if (params != null) {
                this.queryParams.putAll(params);
            }
            return this;
        }

        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            if (headers != null) {
                this.headers.putAll(headers);
            }
            return this;
        }

        public Builder contentType(ContentType contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder requestType(RequestType requestType) {
            this.contentType = requestType.getContentTypeEnum();
            this.headers.put("Accept", requestType.getAccept());
            this.headers.put("Content-Type", requestType.getContentType());
            return this;
        }

        public HttpRequest build() {
            if (endpoint == null || endpoint.isBlank()) {
                throw new IllegalStateException("Endpoint must be set");
            }
            return new HttpRequest(this);
        }
    }
}
