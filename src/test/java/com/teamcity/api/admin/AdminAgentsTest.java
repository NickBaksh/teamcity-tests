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
    public void adminCanGetAllAgentsTest (){
        Agents agents = adminAgentSteps.getAllAgents();
        assertThat(agents).isNotNull();
        assertThat(agents.getAgent()).isNotEmpty();
    }

    @Test
    @DisplayName("Админ может получить агента по ID")
    public void adminCanGetAgentByIdTest(){
        Agent expected = adminAgentSteps.getAllAgents()
                .getAgent()
                .getFirst();
        Agent actual = adminAgentSteps.getAgent(String.valueOf(expected.getId()));

        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getName()).isEqualTo(expected.getName());
    }

    @Test
    @DisplayName("Юзер не может получить несуществующего агента ")
    public void adminCanNotGetNonExistingAgentTest(){
        //Expected: 404 Not Found
    }

    @Test
    @DisplayName("Юзер может включить агента")
    public void adminCanEnableAgentTest(){
        Agent agent = adminAgentSteps.getAllAgents()
                .getAgent()
                .getFirst();

        EnabledInfo enabled = adminAgentSteps.enableAgent(
                String.valueOf(agent.getId())
        );

        assertThat(enabled.getStatus()).isTrue();
    }

    @Test
    @DisplayName("Юзер может выключить агента")
    public void adminCanDisableAgentTest() {
        Agent agent = adminAgentSteps.getAllAgents()
                .getAgent()
                .getFirst();

        try {
            EnabledInfo disabled = adminAgentSteps.disableAgent(String.valueOf(agent.getId()));
            assertThat(disabled.getStatus()).isFalse();
        } finally {
            // включаем агента обратно в любом случае, даже если assert упал
            adminAgentSteps.enableAgent(String.valueOf(agent.getId()));
        }
    }

    @Test
    @DisplayName("Юзер может авторизовать агента")
    public void adminCanAuthorizeAgentTest(){
        Agent agent = adminAgentSteps.getAllAgents()
                .getAgent()
                .getFirst();

        AuthorizedInfo authorized = adminAgentSteps.authorizeAgent(
                String.valueOf(agent.getId())
        );

        assertThat(authorized.getStatus()).isTrue();
    }
}
