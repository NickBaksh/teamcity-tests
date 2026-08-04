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

import java.util.List;

@Slf4j
public class AgentSteps extends BaseSteps {

    public AgentSteps(ApiClient client, ResponseValidator validator) {
        super(client, validator);
    }

    private static final String AGENT_FIELDS =
            "agent(id,name,typeId,connected,authorized,enabled,href,webUrl)";

    @Step("Get all agents")
    public Agents getAllAgents() {
        return getAllAgents("authorized:any");
    }

    @Step("Get agents by locator: {locator}")
    public Agents getAllAgents(String locator) {
        String path = Endpoint.AGENTS.getPath()
                + "?locator=" + locator
                + "&fields=" + AGENT_FIELDS;
        Response response = client.get(path);
        return validator.validate(response, Agents.class);
    }

    @Step("Get connected agents")
    public List<Agent> getConnectedAgents() {
        Agents agents = getAllAgents("connected:true,authorized:any");
        // Locator already restricts to connected; do not re-filter on null fields.
        if (agents.getAgent() == null) {
            return List.of();
        }
        return List.copyOf(agents.getAgent());
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
