package com.teamcity.core.steps;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.ResponseValidator;

public abstract class BaseSteps {

    protected final ApiClient client;
    protected final ResponseValidator validator;

    protected BaseSteps(ApiClient client) {
        this(client, new ResponseValidator());
    }

    protected BaseSteps(ApiClient client, ResponseValidator validator) {
        this.client = client;
        this.validator = validator;
    }
}
