package com.teamcity.core.steps;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.models.Agent;
import com.teamcity.core.models.Agents;
import com.teamcity.core.models.dto.AuthorizedInfo;
import com.teamcity.core.models.dto.EnabledInfo;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentSteps extends BaseSteps {

    public AgentSteps(ApiClient client) {
        super(client);
    }

    public AgentSteps(ApiClient client, ResponseValidator validator) {
        super(client, validator);
    }

    @Step("Get all agents")
    public Agents getAllAgents() {
        Response response = client.get(
                Endpoint.AGENTS.getPath());

        return validator.validate(response, Agents.class);
    }

    @Step("Get agent: {agentId}")
    public Agent getAgent(String agentId) {
        Response response = client.get(
                Endpoint.AGENT.format("id:" + agentId));

        return validator.validate(response, Agent.class);
    }

    @Step("Enable agent: {agentId}")
    public EnabledInfo enableAgent(String agentId) {

        EnabledInfo request = EnabledInfo.builder()
                .status(true)
                .build();

        Response response = client.put(
                Endpoint.AGENT_ENABLED_INFO.format("id:" + agentId),
                request);

        EnabledInfo enabled = validator.validate(response, EnabledInfo.class);

        log.info("Agent {} enabled", agentId);

        return enabled;
    }

    @Step("Disable agent: {agentId}")
    public EnabledInfo disableAgent(String agentId) {

        EnabledInfo request = EnabledInfo.builder()
                .status(false)
                .build();

        Response response = client.put(
                Endpoint.AGENT_ENABLED_INFO.format("id:" + agentId),
                request);

        EnabledInfo disabled = validator.validate(response, EnabledInfo.class);

        log.info("Agent {} disabled", agentId);

        return disabled;
    }

    @Step("Authorize agent: {agentId}")
    public AuthorizedInfo authorizeAgent(String agentId) {
        AuthorizedInfo request = AuthorizedInfo.builder()
                .status(true)
                .build();

        Response response = client.put(
                Endpoint.AGENT_AUTHORIZED_INFO.format("id:" + agentId),
                request);

        AuthorizedInfo authorized = validator.validate(response, AuthorizedInfo.class);

        log.info("Agent {} authorized", agentId);

        return authorized;
    }
}