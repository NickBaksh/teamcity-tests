package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.models.Agent;
import com.teamcity.core.models.Agents;
import com.teamcity.core.models.dto.AuthorizedInfo;
import com.teamcity.core.models.dto.EnabledInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminAgentsTest extends BaseApiTest {

    @Test
    @DisplayName("Админ может получить всех агентов")
    void adminCanGetAllAgentsTest() {

        Agents agents = agentSteps(adminClient()).getAll();

        assertThat(agents).isNotNull();
        assertThat(agents.getAgent()).isNotEmpty();
    }

    @Test
    @DisplayName("Админ может получить агента по ID")
    void adminCanGetAgentByIdTest() {

        Agent expected = agentSteps(adminClient())
                .getAll()
                .getAgent()
                .getFirst();

        Agent actual = agentSteps(adminClient())
                .get(expected.getId());

        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getName()).isEqualTo(expected.getName());
    }

    @Test
    @DisplayName("Админ не может получить несуществующего агента")
    void adminCanNotGetNonExistingAgentTest() {
        // TODO: 404 Not Found
    }

    @Test
    @DisplayName("Админ может включить агента")
    void adminCanEnableAgentTest() {

        Agent agent = agentSteps(adminClient())
                .getAll()
                .getAgent()
                .getFirst();

        EnabledInfo enabled = agentSteps(adminClient())
                .enable(agent);

        assertThat(enabled.getStatus()).isTrue();
    }

    @Test
    @DisplayName("Админ может выключить агента")
    void adminCanDisableAgentTest() {

        Agent agent = agentSteps(adminClient())
                .getAll()
                .getAgent()
                .getFirst();

        try {

            EnabledInfo disabled = agentSteps(adminClient())
                    .disable(agent);

            assertThat(disabled.getStatus()).isFalse();

        } finally {

            agentSteps(adminClient())
                    .enable(agent);
        }
    }

    @Test
    @DisplayName("Админ может авторизовать агента")
    void adminCanAuthorizeAgentTest() {

        Agent agent = agentSteps(adminClient())
                .getAll()
                .getAgent()
                .getFirst();

        AuthorizedInfo authorized = agentSteps(adminClient())
                .authorize(agent);

        assertThat(authorized.getStatus()).isTrue();
    }
}