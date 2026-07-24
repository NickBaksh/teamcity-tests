package com.teamcity.ui.admin;

import com.teamcity.core.annotations.RestoreAgent;
import com.teamcity.core.models.Agent;
import com.teamcity.core.models.Agents;
import com.teamcity.ui.BaseUiTest;
import com.teamcity.ui.extensions.AdminUiSessionExtension;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("UI Agents Management")
@Tag("ui")
@Tag("admin")
@ExtendWith(AdminUiSessionExtension.class)
public class AdminAgentsUiTest extends BaseUiTest {

    @Test
    @Severity(SeverityLevel.NORMAL)
    void adminCanViewAgents() {
        agentsPage
                .openPage()
                .shouldBeOpened()
                .shouldHaveAgents();

        Agents agents = agentSteps.getAllAgents();

        assertThat(agentsPage.getAgentsCount()).isEqualTo(agents.getAgent().size());
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @RestoreAgent
    void adminCanDisableAgent() {
        Agent agent = givenTrackedAgent();

        agentsPage
                .openPage()
                .disableAgent(agent.getName())
                .confirmDisableAgent();

        assertThat(agentSteps.getAgent(String.valueOf(agent.getId())).getEnabled())
                .isFalse();
    }
}
