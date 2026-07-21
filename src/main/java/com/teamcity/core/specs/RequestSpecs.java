package com.teamcity.core.specs;

import com.teamcity.core.config.ConfigManager;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class RequestSpecs {

    private RequestSpecs() {
    }

    public static RequestSpecification base() {
        return defaultBuilder().build();
    }

    public static RequestSpecification withBasicAuth(String username, String password) {
        PreemptiveBasicAuthScheme auth = new PreemptiveBasicAuthScheme();
        auth.setUserName(username);
        auth.setPassword(password);
        return defaultBuilder()
                .setAuth(auth)
                .build();
    }

    public static RequestSpecification withBearerToken(String token) {
        return defaultBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }

    public static RequestSpecification admin() {
        return withBasicAuth(ConfigManager.getAdminLogin(), ConfigManager.getAdminPassword());
    }

    private static RequestSpecBuilder defaultBuilder() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigManager.getApiBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setRelaxedHTTPSValidation()
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new AllureRestAssured());
    }
}
