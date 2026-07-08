package com.teamcity.core.client;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ApiRequest {
    private final Method method;
    private final String endpoint;
    private final Object body;
    private final Map<String, String> pathParams;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;
    private final ContentType contentType;
    private final boolean requireAuthentication;

    public enum Method {
        GET, POST, PUT, DELETE, PATCH
    }

    public enum ContentType {
        JSON("application/json"),
        TEXT("text/plain"),
        XML("application/xml");

        private final String value;

        ContentType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static class ApiRequestBuilder {
        // Builder будет сгенерирован Lombok
    }
}