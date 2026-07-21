package com.teamcity.core.client;

import java.util.HashMap;
import java.util.Map;

public class HeaderConfig {
    private final Map<String, String> headers = new HashMap<>();

    private HeaderConfig() {
        this.headers.put("Accept", ContentType.JSON.getValue());
        this.headers.put("Content-Type", ContentType.JSON.getValue());
    }

    public static HeaderConfig defaultHeaders() {
        return new HeaderConfig();
    }

    public HeaderConfig withAccept(ContentType contentType) {
        this.headers.put("Accept", contentType.getValue());
        return this;
    }

    public HeaderConfig withAccept(String contentType) {
        this.headers.put("Accept", contentType);
        return this;
    }

    public HeaderConfig withContentType(ContentType contentType) {
        this.headers.put("Content-Type", contentType.getValue());
        return this;
    }

    public HeaderConfig withContentType(String contentType) {
        this.headers.put("Content-Type", contentType);
        return this;
    }

    public HeaderConfig withCustomHeader(String key, String value) {
        if (key != null && value != null) {
            this.headers.put(key, value);
        }
        return this;
    }

    public HeaderConfig withCustomHeaders(Map<String, String> customHeaders) {
        if (customHeaders != null && !customHeaders.isEmpty()) {
            this.headers.putAll(customHeaders);
        }
        return this;
    }

    public HeaderConfig withRequestType(RequestType requestType) {
        this.headers.put("Accept", requestType.getAccept());
        this.headers.put("Content-Type", requestType.getContentType());
        return this;
    }

    public Map<String, String> build() {
        return new HashMap<>(this.headers);
    }

    @Override
    public String toString() {
        return "HeaderConfig{" + headers + '}';
    }
}
