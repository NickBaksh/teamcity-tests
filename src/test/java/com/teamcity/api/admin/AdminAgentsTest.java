package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.models.Agent;
import com.teamcity.core.models.Agents;
import com.teamcity.core.models.dto.AuthorizedInfo;
import com.teamcity.core.models.dto.EnabledInfo;
import com.teamcity.core.testdata.TestDataValues;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Agents Management")
@Tag("admin")
public class AdminAgentsTest extends BaseApiTest {

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetAllAgents() {
        Agents agents = agentSteps.getAllAgents();
        assertThat(agents).isNotNull();
        assertThat(agents.getAgent()).isNotEmpty();
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetAgentById() {
        Agent expected = givenAgent();
        Agent actual = agentSteps.getAgent(expected.getId().toString());
        ApiAssertions.assertAgentsEqual(expected, actual);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404ForNonExistingAgent() {
        ApiAssertions.assertNotFound(
                () -> agentSteps.getAgent(TestDataValues.NON_EXISTENT_AGENT_ID)
        );
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn400ForInvalidAgentId() {
        ApiAssertions.assertBadRequest(
                () -> agentSteps.getAgent(TestDataValues.INVALID_AGENT_ID)
        );
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldEnableAgent() {
        EnabledInfo enabled = agentSteps.enableAgent(givenAgent().getId().toString());
        ApiAssertions.assertAgentEnabled(enabled);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldDisableAgent() {
        Agent agent = givenAgent();
        EnabledInfo disabled = agentSteps.disableAgent(agent.getId().toString());
        ApiAssertions.assertAgentDisabled(disabled);
        agentSteps.enableAgent(agent.getId().toString());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldAuthorizeAgent() {
        AuthorizedInfo authorized = agentSteps.authorizeAgent(givenAgent().getId().toString());
        ApiAssertions.assertAgentAuthorized(authorized);
    }
}
