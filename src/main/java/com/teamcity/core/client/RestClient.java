package com.teamcity.core.client;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestClient {
    private final RequestSpecification requestSpec;

    private RestClient(Builder builder) {
        RestAssured.baseURI = builder.baseUrl;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        RequestSpecification spec = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .relaxedHTTPSValidation()
                .filters(
                        new RequestLoggingFilter(),
                        new ResponseLoggingFilter(),
                        new AllureRestAssured()
                );

        // Если передан токен — используем Bearer Auth
        if (builder.token != null && !builder.token.isEmpty()) {
            log.info("Using Bearer Token authentication");
            spec.header("Authorization", "Bearer " + builder.token);
        } else {
            // Fallback на Basic Auth
            log.info("Using Basic authentication");
            spec.auth().basic(builder.username, builder.password);
        }

        this.requestSpec = spec;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Response get(String endpoint, Object... pathParams) {
        log.info("GET request to: {}", endpoint);
        return requestSpec
                .when()
                .get(endpoint, pathParams)
                .then()
                .extract().response();
    }

    public Response post(String endpoint, Object body) {
        log.info("POST request to: {}", endpoint);
        return requestSpec
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract().response();
    }

    public Response post(String endpoint, Object body, Object... pathParams) {
        log.info("POST request to: {}", endpoint);
        return requestSpec
                .body(body)
                .when()
                .post(endpoint, pathParams)
                .then()
                .extract().response();
    }

    public Response put(String endpoint, Object body, Object... pathParams) {
        log.info("PUT request to: {}", endpoint);
        return requestSpec
                .contentType("text/plain")
                //.accept(ContentType.JSON)
                .body(body)
                .when()
                .put(endpoint, pathParams)
                .then()
                .extract().response();
    }

    public Response delete(String endpoint, Object... pathParams) {
        log.info("DELETE request to: {}", endpoint);
        return requestSpec
                .when()
                .delete(endpoint, pathParams)
                .then()
                .extract().response();
    }

    public static class Builder {
        private String baseUrl;
        private String username;
        private String password;
        private String token;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder basicAuth(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        public Builder bearerToken(String token) {
            this.token = token;
            return this;
        }

        public RestClient build() {
            if (baseUrl == null) {
                throw new IllegalStateException("Base URL must be set");
            }
            return new RestClient(this);
        }
    }
}