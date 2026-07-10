package com.teamcity.core.steps;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.ResponseValidator;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

public class BuildFeatureSteps {

    private final ApiClient client;
    private final ResponseValidator validator;


    public BuildFeatureSteps(ApiClient client) {
        this.client = client;
        this.validator = new ResponseValidator();
    }


    @Step("Add command line build step")
    public void addCommandLineStep(String buildTypeId, String script) {

        Map<String, Object> body = Map.of(
                "step",
                Map.of(
                        "name", "Create artifact",
                        "type", "simpleRunner",
                        "properties", Map.of(
                                "property", List.of(
                                        Map.of(
                                                "name", "script.content",
                                                "value", script
                                        )
                                )
                        )
                )
        );


        Response response = client.post(
                String.format(
                        "/app/rest/buildTypes/id:%s/steps",
                        buildTypeId
                ),
                body
        );

        validator.validateStatus(response);
    }
}
