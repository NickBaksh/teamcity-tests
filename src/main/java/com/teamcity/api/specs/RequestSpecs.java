package com.teamcity.api.specs;

import com.teamcity.api.configs.Config;
import com.teamcity.common.extensions.AdminSessionExtension;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.List;

public class RequestSpecs {
    private RequestSpecs() {}

    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(
                        new RequestLoggingFilter(),
                        new ResponseLoggingFilter()
                ))
                .setBaseUri(Config.getApiBaseUrl());
    }

    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification adminSpec() {
        String authHeader = AdminSessionExtension.getAdminAuthHeader();
        return defaultRequestBuilder()
                .addHeader("Authorization", authHeader)
                .build();
    }

    public static RequestSpecification authWithTokenSpec(String token) {
        return defaultRequestBuilder()
                .addHeader("Authorization", token)
                .build();
    }

    public static RequestSpecification authWithBasicSpec(String username, String password) {
        String credentials = username + ":" + password;
        String encoded = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
        return defaultRequestBuilder()
                .addHeader("Authorization", "Basic " + encoded)
                .build();
    }
}