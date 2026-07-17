package com.teamcity.core.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;

public final class ResponseSpecs {

    private ResponseSpecs() {
    }

    public static ResponseSpecification ok() {
        return status(HttpStatus.SC_OK);
    }

    public static ResponseSpecification created() {
        return status(HttpStatus.SC_CREATED);
    }

    public static ResponseSpecification noContent() {
        return status(HttpStatus.SC_NO_CONTENT);
    }

    public static ResponseSpecification badRequest() {
        return status(HttpStatus.SC_BAD_REQUEST);
    }

    public static ResponseSpecification unauthorized() {
        return status(HttpStatus.SC_UNAUTHORIZED);
    }

    public static ResponseSpecification forbidden() {
        return status(HttpStatus.SC_FORBIDDEN);
    }

    public static ResponseSpecification notFound() {
        return status(HttpStatus.SC_NOT_FOUND);
    }

    public static ResponseSpecification conflict() {
        return status(HttpStatus.SC_CONFLICT);
    }

    public static ResponseSpecification serverError() {
        return status(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    public static ResponseSpecification status(int statusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(statusCode)
                .build();
    }
}
