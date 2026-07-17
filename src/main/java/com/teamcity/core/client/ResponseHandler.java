package com.teamcity.core.client;

import io.restassured.response.Response;

@FunctionalInterface
public interface ResponseHandler<T> {
    T handle(Response response);
}
