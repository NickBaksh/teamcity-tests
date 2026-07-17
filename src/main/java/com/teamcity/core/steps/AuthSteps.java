package com.teamcity.core.steps;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.ClientFactory;
import com.teamcity.core.client.HttpStatusCodes;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.exceptions.ApiException;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthSteps extends BaseSteps {

    public AuthSteps(ApiClient client) {
        super(client);
    }

    public AuthSteps(ApiClient client, ResponseValidator validator) {
        super(client, validator);
    }

    @Step("Verify API is accessible with current credentials")
    public Response verifyServerAccessible() {
        Response response = client.get(Endpoint.SERVER.getPath());
        validator.validateStatus(response);
        log.info("Server is accessible, status={}", response.statusCode());
        return response;
    }

    @Step("Verify credentials are rejected")
    public void verifyCredentialsRejected(String username, String password) {
        ApiClient invalidClient = ClientFactory.createBasicAuthClient(username, password);
        try {
            invalidClient.get(Endpoint.SERVER.getPath());
            throw new AssertionError("Expected authentication failure for user: " + username);
        } catch (ApiException e) {
            if (e.getStatusCode() != HttpStatusCodes.UNAUTHORIZED
                    && e.getStatusCode() != HttpStatusCodes.FORBIDDEN) {
                throw e;
            }
            log.info("Credentials rejected as expected: status={}", e.getStatusCode());
        }
    }

    @Step("Verify invalid auth client is rejected")
    public void verifyInvalidAuthRejected() {
        ApiClient invalidClient = ClientFactory.createInvalidAuthClient();
        try {
            invalidClient.get(Endpoint.SERVER.getPath());
            throw new AssertionError("Expected authentication failure for invalid credentials");
        } catch (ApiException e) {
            if (e.getStatusCode() != HttpStatusCodes.UNAUTHORIZED
                    && e.getStatusCode() != HttpStatusCodes.FORBIDDEN) {
                throw e;
            }
            log.info("Invalid auth rejected as expected: status={}", e.getStatusCode());
        }
    }
}
