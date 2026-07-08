package com.teamcity.core.steps;

import com.teamcity.api.specs.ResponseSpecs;
import com.teamcity.core.client.RestClient;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.models.Agent;
import com.teamcity.core.models.Agents;
import com.teamcity.core.models.dto.AuthorizedInfo;
import com.teamcity.core.models.dto.EnabledInfo;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentSteps {
    private final RestClient client;

    public AgentSteps(RestClient client) {
        this.client = client;
    }

    @Step("Get all agents")
    public Agents getAllAgents() {
        Response response = client.get(Endpoint.AGENTS.getPath());

        response.then().spec(ResponseSpecs.requestReturnsOK());
        return response.as(Agents.class);
    }

    @Step("Get agent by ID: {agentId}")
    public Agent getAgent(String agentId) {
        Response response = client.get(
                Endpoint.AGENT.getPath(),
                agentId);

        response.then().spec(ResponseSpecs.requestReturnsOK());
        return response.as(Agent.class);
    }

    @Step("Enable agent: {agentId}")
    public EnabledInfo enableAgent(String agentId) {

        EnabledInfo request = EnabledInfo.builder()
                .status(true)
                .build();

        Response response = client.put(
                Endpoint.AGENT_ENABLED_INFO.getPath(),
                request,
                agentId);

        response.then().spec(ResponseSpecs.requestReturnsOK());
        EnabledInfo enabledInfo = response.as(EnabledInfo.class);
        log.info("Agent enabled: ID={}", agentId);
        return enabledInfo;
    }

    @Step("Disable agent: {agentId}")
    public EnabledInfo disableAgent(String agentId) {

        EnabledInfo request = EnabledInfo.builder()
                .status(false)
                .build();

        Response response = client.put(
                Endpoint.AGENT_ENABLED_INFO.getPath(),
                request,
                agentId);

        response.then().spec(ResponseSpecs.requestReturnsOK());
        EnabledInfo enabledInfo = response.as(EnabledInfo.class);
        log.info("Agent disabled: ID={}", agentId);
        return enabledInfo;
    }

    @Step("Authorize agent: {agentId}")
    public AuthorizedInfo authorizeAgent(String agentId) {

        AuthorizedInfo request = AuthorizedInfo.builder()
                .status(true)
                .build();

        Response response = client.put(
                Endpoint.AGENT_AUTHORIZED_INFO.getPath(),
                request,
                agentId);

        response.then().spec(ResponseSpecs.requestReturnsOK());
        AuthorizedInfo authorizedInfo = response.as(AuthorizedInfo.class);
        log.info("Agent authorized: ID={}", agentId);
        return authorizedInfo;
    }
}