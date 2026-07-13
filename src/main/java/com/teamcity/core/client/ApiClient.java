package com.teamcity.core.client;

import io.restassured.response.Response;

import java.util.Map;

public interface ApiClient {

    Response get(String endpoint, Object... pathParams);

    Response post(String endpoint, Object body);

    Response post(String endpoint, Object body, Object... pathParams);

    Response put(String endpoint, Object body, Object... pathParams);

    Response delete(String endpoint, Object... pathParams);

    <T> T execute(HttpRequest request, Class<T> responseType);

    <T> T execute(HttpRequest request, ResponseHandler<T> handler);

    Response putText(String endpoint, String body, Object... pathParams);

    Response putBoolean(String endpoint, boolean body, Object... pathParams);

    Response get(String endpoint, Map<String, String> headers, Object... pathParams);

    Response post(String endpoint, Object body, Map<String, String> headers, Object... pathParams);

    Response put(String endpoint, Object body, Map<String, String> headers, Object... pathParams);

    Response delete(String endpoint, Map<String, String> headers, Object... pathParams);

    Response get(String endpoint, RequestType requestType, Object... pathParams);

    Response post(String endpoint, Object body, RequestType requestType, Object... pathParams);

    Response put(String endpoint, Object body, RequestType requestType, Object... pathParams);

    default Response getText(String endpoint, Object... pathParams) {
        return get(endpoint, RequestType.TEXT_ACCEPT_JSON, pathParams);
    }
}
