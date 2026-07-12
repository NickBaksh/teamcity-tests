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

    // =========================================================================
    // GET
    // =========================================================================

    @Step("Get all agents")
    public Agents getAll() {

        Response response = client.get(
                Endpoint.AGENTS.getPath()
        );

        response.then().spec(ResponseSpecs.requestReturnsOK());

        return response.as(Agents.class);
    }

    @Step("Get agent: {agentId}")
    public Agent get(Integer agentId) {

        Response response = client.get(
                Endpoint.AGENT.format("id:" + agentId)
        );

        response.then().spec(ResponseSpecs.requestReturnsOK());

        return response.as(Agent.class);
    }

    public Agent get(Agent agent) {
        return get(agent.getId());
    }

    // =========================================================================
    // ENABLE / DISABLE
    // =========================================================================

    @Step("Enable agent: {agentId}")
    public EnabledInfo enable(Integer agentId) {

        EnabledInfo request = EnabledInfo.builder()
                .status(true)
                .build();

        Response response = client.put(
                Endpoint.AGENT_ENABLED_INFO.format("id:" + agentId),
                request
        );

        response.then().spec(ResponseSpecs.requestReturnsOK());

        log.info("Agent {} enabled", agentId);

        return response.as(EnabledInfo.class);
    }

    public EnabledInfo enable(Agent agent) {
        return enable(agent.getId());
    }

    @Step("Disable agent: {agentId}")
    public EnabledInfo disable(Integer agentId) {

        EnabledInfo request = EnabledInfo.builder()
                .status(false)
                .build();

        Response response = client.put(
                Endpoint.AGENT_ENABLED_INFO.format("id:" + agentId),
                request
        );

        response.then().spec(ResponseSpecs.requestReturnsOK());

        log.info("Agent {} disabled", agentId);

        return response.as(EnabledInfo.class);
    }

    public EnabledInfo disable(Agent agent) {
        return disable(agent.getId());
    }

    // =========================================================================
    // AUTHORIZE
    // =========================================================================

    @Step("Authorize agent: {agentId}")
    public AuthorizedInfo authorize(Integer agentId) {

        AuthorizedInfo request = AuthorizedInfo.builder()
                .status(true)
                .build();

        Response response = client.put(
                Endpoint.AGENT_AUTHORIZED_INFO.format("id:" + agentId),
                request
        );

        response.then().spec(ResponseSpecs.requestReturnsOK());

        log.info("Agent {} authorized", agentId);

        return response.as(AuthorizedInfo.class);
    }

    public AuthorizedInfo authorize(Agent agent) {
        return authorize(agent.getId());
    }
}